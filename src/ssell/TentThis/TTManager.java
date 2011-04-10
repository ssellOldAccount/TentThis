package ssell.TentThis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class TTManager 
{
	private static final Logger log = Logger.getLogger( "Minecraft" );
	
	public List< TTTent > tentList = new ArrayList< TTTent >( );
	public List< TTPlayer > playerList = new ArrayList< TTPlayer >( );
	
	private final TentThis plugin;
	
	public int globalLimit = -1;
	public String defaultSchema = "default";
	
	public TTManager( TentThis instance )
	{
		plugin = instance;
	}
	
	//--------------------------------------------------------------------------------------
	// Tent Methods
	
	/**
	 * Returns whether or not the specified text is already loaded in.
	 */
	public boolean exists( String name )
	{
		for( int i = 0; i < tentList.size( ); i++ )
		{
			if( tentList.get( i ).schemaName.equalsIgnoreCase( name ) )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the specified tent. If it can not be found, returns null.
	 * 
	 * @param name
	 * @return
	 */
	public TTTent getTent( String name )
	{
		if( exists( name ) )
		{
			for( int i = 0; i < tentList.size( ); i++ )
			{
				if( tentList.get( i ).schemaName.equalsIgnoreCase( name ) )
				{
					return tentList.get( i );
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Creates the specified tent.
	 * 
	 * @param name
	 */
	public void createTent( String name )
	{
		//Don't waste time making it if it already exists
		if( exists( name ) )
		{
			return;
		}
		
		tentList.add( plugin.schemaLoader.createTent( name ) );
	}
	
	//--------------------------------------------------------------------------------------
	// Player Methods
	
	/**
	 * Returns whether or not the specified player is currently tracked.
	 */
	public boolean isTracked( String name )
	{
		//First check playerList
		for( int i = 0; i < playerList.size( ); i++ )
		{
			if( playerList.get( i ).name.equalsIgnoreCase( name ) )
			{
				return true;
			}
		}
		
		//Player may still be in the hard copy
		Scanner scanner;
		
		try 
		{
			scanner = new Scanner( new FileReader( "plugins/TentThis/players.txt" ) );
		} 
		catch( FileNotFoundException e ) 
		{
			log.info( "TentThis: Could not find 'plugins/TentThis/players.txt!" );
			
			return false;
		}
		
		while( scanner.hasNext( ) )
		{
			String nameStr = scanner.next( );
			String schemaStr = scanner.next( );
			
			int limit = Integer.parseInt( scanner.next( ) );
			
			if( nameStr.equalsIgnoreCase( name ) )
			{
				TTPlayer player = new TTPlayer( );
				
				player.name = name;
				player.currentTent = getTent( schemaStr );
				player.limit = limit;
				
				playerList.add( player );
				
				scanner.close( );
				
				return true;
			}
		}
		
		scanner.close( );
		
		addPlayer( name );
		
		return isTracked( name );
	}
	
	/**
	 * Adds a new player with the default information.
	 * 
	 * @param name
	 */
	public void addPlayer( String name )
	{
		TTPlayer player = new TTPlayer( );
		
		player.name = name;
		player.currentTent = tentList.get( 0 );
		player.limit = globalLimit;
		
		playerList.add( player );
		
		//Add to file
		try 
		{
			BufferedWriter out = new BufferedWriter(new FileWriter( "plugins/TentThis/players.txt", true ) );
			
			out.newLine( );
			out.write( player.name + " " + player.currentTent.schemaName + " " + player.limit );
			
			out.close( );
		} 
		catch( IOException e ) 
		{
			log.info( "TentThis: Failed to write to 'plugins/TentThis/players.txt'!" );
		}
	}
	
	public void savePlayer( String name )
	{		
		TTPlayer player = getPlayer( name );
		
		File inputFile = new File( "plugins/TentThis/players.txt" );
		File tempFile = new File( "plugins/TentThis/players.tmp" );

		BufferedReader reader;
		BufferedWriter writer;
		
		try 
		{
			reader = new BufferedReader( new FileReader( inputFile ) );
		} 
		catch( FileNotFoundException e ) 
		{
			log.info( "TentThis: Failed to save '" + name + "' to 'plugins/TentThis/players.txt'!" );
			return;
		}
		
		try 
		{
			writer = new BufferedWriter( new FileWriter( tempFile ) );
		} 
		catch( IOException e ) 
		{
			log.info( "TentThis: Failed to save '" + name + "' to 'plugins/TentThis/players.txt'!" );
			return;
		}
		
		String currentLine = "";

		while( currentLine != null )
		{
			try 
			{
				currentLine = reader.readLine( );
			} 
			catch( IOException e1 ) 
			{
				log.info( "TentThis: Failed to save '" + name + "' to 'plugins/TentThis/players.txt'!" );
				return;
			}
			
			if( currentLine == null )
			{
				break;
			}
		    
		    if( currentLine.contains( player.name ) ) 
		    {
		    	try 
		    	{
		    		writer.newLine( );
					writer.write( player.name + " " + player.currentTent.schemaName + " " + player.limit );
				} 
		    	catch( IOException e ) 
		    	{
		    		log.info( "TentThis: Failed to save '" + name + "' to 'plugins/TentThis/players.txt'!" );
					return;
				}
		    	
		    	continue;
		    }
		    
		    try 
		    {
		    	writer.newLine( );
				writer.write( currentLine );
			} 
		    catch( IOException e ) 
		    {
				e.printStackTrace();
			}
		}
		
		try 
		{
			writer.close( );
			reader.close( );
		} 
		catch( IOException e ) 
		{
			log.info( "TentThis: Failed to save '" + name + "' to 'plugins/TentThis/players.txt'!" );
			return;
		}
		
		inputFile.delete( );
		
		tempFile.renameTo( new File( "plugins/TentThis/players.txt" ) );

	}
	
	public void saveAll( )
	{
		for( int i = 0; i < playerList.size( ); i++ )
		{
			savePlayer( playerList.get( i ).name );
		}
	}
	
	/**
	 * Returns the player if they are found.
	 * 
	 * @param name
	 * @return
	 */
	public TTPlayer getPlayer( String name )
	{
		if( isTracked( name ) )
		{
			for( int i = 0; i < playerList.size( ); i++ )
			{
				if( playerList.get( i ).name.equalsIgnoreCase( name ) )
				{
					return playerList.get( i );
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Sets the tent limit for either the player or everyone.
	 * 
	 * @param limit
	 * @param name
	 * @return
	 */
	public boolean setLimit( int limit, String name )
	{
		if( name.equalsIgnoreCase( "-all" ) )
		{
			for( int i = 0; i < playerList.size( ); i++ )
			{
				playerList.get( i ).limit = limit;
				playerList.get( i ).update( );
			}
			
			return true;
		}
		else
		{
			TTPlayer player = getPlayer( name );
			
			if( player != null )
			{
				player.limit = limit;
				player.update( );
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Sets the schema for the specified player, or everyone.
	 * 
	 * @param schema
	 * @param who
	 * @return
	 */
	public boolean setSchema( String schema, String who )
	{
		if( exists( schema ) )
		{
			TTTent tent = getTent( schema );
			
			if( who.equalsIgnoreCase( "-all" ) )
			{
				for( int i = 0; i < playerList.size( ); i++ )
				{
					playerList.get( i ).currentTent = tent;
				}
				
				return true;
			}
			else
			{
				TTPlayer player = getPlayer( who );
				
				if( player != null )
				{
					player.currentTent = tent;
					
					return true;
				}
			}
		}
		else
		{
			//Schema does not currently exist.
			//But it may be new, so check the .properties
			Scanner scanner;
			
			try 
			{
				scanner = new Scanner( new FileReader( "plugins/TentThis/TentThis.properties" ) );
			} 
			catch( FileNotFoundException e ) 
			{
				log.info( "TentThis: Failed to find 'plugins/TentThis/TentThis.properties'! [LoadSchema]" );
				return false;
			}
			
			while( scanner.hasNext( ) )
			{
				String string = scanner.next( );
				
				if( string.contains( "<tentSchema=" ) )
				{
					if( string.contains( schema ) )
					{
						createTent( schema );
						
						setSchema( schema, who );
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Destroys the provided tent.
	 * @param tentBlocks
	 * @return
	 */
	public boolean destroyTent( List< Block > tentBlocks )
	{
		for( int i = 0; i < playerList.size( ); i++ )
		{
			TTPlayer player = playerList.get( i );
			
			for( int j = 0; j < player.tentList.size( ); j++ )
			{
				if( player.tentList.equals( tentBlocks ) )
				{
					//deleteTent( );
					
					player.tentList.remove( j );
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	//--------------------------------------------------------------------------------------
	// Misc
	
	public TTPlayer whoOwnsThis( Block block )
	{
		TTPlayer player = null;
		
		for( int i = 0; i < playerList.size( ); i++ )
		{
			player = playerList.get( i );
			
			for( int j = 0; j < player.tentList.size( ); j++ )
			{
				if( player.tentList.get( j ).second.contains( block ) )
				{
					return player;
				}
			}
		}
		
		return null;
	}
	
	public boolean isDestructionBlock( int material )
	{
		for( int i = 0; i < tentList.size( ); i++ )
		{
			if( tentList.get( i ).destructionBlock == material )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean destructionBlockBelongToSchema( String schema, int material )
	{
		for( int i = 0; i < tentList.size( ); i++ )
		{
			if( tentList.get( i ).schemaName.equalsIgnoreCase( schema ) )
			{
				if( tentList.get( i ).destructionBlock == material )
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		
		return false;
	}
}
