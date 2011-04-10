package ssell.TentThis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TTReverseSchema 
{
	private static final Logger log = Logger.getLogger( "Minecraft" );
	
	private final TentThis plugin;
	
						//     Name     Corners      Schema Title
	private List< ReversePlayer > playerList = new ArrayList< ReversePlayer >( );
	
	public TTReverseSchema( TentThis instance )
	{
		plugin = instance;
	}
	
	//--------------------------------------------------------------------------------------
	
	/**
	 * Finds if the specified player is already being tracked and waited on.
	 * If found, returns index location. Else returns -1.
	 */
	public int isTracked( String name )
	{
		for( int i = 0; i < playerList.size( ); i++ )
		{
			if( playerList.get( i ).name.equalsIgnoreCase( name ) )
			{
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Adds a new player to the playerList. If the list
	 * already contains the player, the old record is removed.
	 * 
	 * @param player
	 */
	public void waitForPlayer( String name, String schemaTitle, int cornerToIgnore, int destructionBlock )
	{
		//If the player is already tracked, remove the old record
		int pos = isTracked( name );
		
		if( pos != -1 )
		{
			playerList.remove( pos );
		}
		
		playerList.add( new ReversePlayer( name, schemaTitle, cornerToIgnore, destructionBlock ) );
	}
	
	/**
	 * Removes the player from the playerList if they are on it.
	 * 
	 * @param player
	 */
	public void stopTracking( String name )
	{
		int pos = isTracked( name );
		
		if( pos != -1 )
		{
			playerList.remove( pos );
		}
	}
	
	/**
	 * Adds the block to the player's blockList.<br><br>
	 * 
	 * If two blocks have been hit, it stops tracking 
	 * and begins making the schema.
	 * 
	 * @param name
	 * @param block
	 */
	public void updatePlayer( String name, Block block )
	{
		int pos = isTracked( name );
		
		if( pos != -1 )
		{
			ReversePlayer player = playerList.get( pos );
			
			plugin.getServer( ).getPlayer( name ).sendMessage( ChatColor.GREEN +
					"TentThis Reverse Schema: Corner " + 
					( player.blockList.size( ) + 1 ) + ": " +
					block.getType( ).toString( ) + " " + block.getLocation( ).toString( ) );
			
			playerList.get( pos ).blockList.add( block );
			
			//Hit the two blocks
			if( player.blockList.size( ) == 2 )
			{		
				if( ( player.cornerToIgnore == 2 ) || ( player.cornerToIgnore == 3 ) )
				{
					player.cornerTwo = block;
				}
				
				grabBlocks( player );
				
				stopTracking( name );
			}
			else
			{
				if( ( player.cornerToIgnore == 1 ) || ( player.cornerToIgnore == 3 ) )
				{
					player.cornerOne = block;
				}
			}
		}
	}
	
	//--------------------------------------------------------------------------------------
	
	/**
	 * The first step in making a reverse schema.<br>
	 * Grabs all the blocks in the specified area.
	 */
	private void grabBlocks( ReversePlayer player )
	{
		//The area is inclusive.
		Block blockA = player.blockList.get( 0 );
		Block blockB = player.blockList.get( 1 );
		
		int aX, aY, aZ;
		int bX, bY, bZ;
		
		World world = blockA.getWorld( );
		
		//----------------------------------------------------------------------------------
		// Get the correct corners
		
		if( blockA.getX( ) < blockB.getX( ) )
		{
			aX = blockA.getX( );
			bX = blockB.getX( );
		}
		else
		{
			aX = blockB.getX( );
			bX = blockA.getX( );
		}
		
		if( blockA.getY( ) < blockB.getY( ) )
		{
			aY = blockA.getY( );
			bY = blockB.getY( );
		}
		else
		{
			aY = blockB.getY( );
			bY = blockA.getY( );
		}
		
		if( blockA.getZ( ) < blockB.getZ( ) )
		{
			aZ = blockA.getZ( );
			bZ = blockB.getZ( );
		}
		else
		{
			aZ = blockB.getZ( );
			bZ = blockA.getZ( );
		}	
		
		blockA = world.getBlockAt( aX, aY, aZ );
		blockB = world.getBlockAt( bX, bY, bZ );
		
		//Get the dimensions
		
		int length = blockB.getZ( ) - blockA.getZ( ) + 1;
		int width = blockB.getX( ) - blockA.getX( ) + 1;
		int height = blockB.getY( ) - blockA.getY( ) + 1;
				
		//----------------------------------------------------------------------------------
		// Grab the blocks
		
		List< Block > blockList = new ArrayList< Block >( );
		
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				for( int z = 0; z < length; z++ )
				{
					blockList.add( world.getBlockAt( blockA.getX( ) + x,
													 blockA.getY( ) + y,
													 blockA.getZ( ) + z ) );
				}
			}
		}
		
		//----------------------------------------------------------------------------------
		
		player.blockList = blockList;
			
		createSchema( player, length, width, height );
		
	}
	
	private void createSchema( ReversePlayer player, int length, int width, int height )
	{
		/*
		 * Explanation about yIncrement:
		 * 
		 * Since no order is specified to the player when selecting corners,
		 * they could select the top corner before the bottom.
		 * 
		 * With the yIncrement, calculated in grabBlocks, the plugin knows
		 * whether to increment through the list, or decrement backwards.
		 */
		
		BufferedWriter writer;
		
		int cntr = 0;
		
		try 
		{
			writer = new BufferedWriter( new FileWriter( "plugins/TentThis/TentThis.properties", true ) );
		} 
		catch( IOException e ) 
		{
			log.info( "TentThis: Failed to open 'plugins/TentThis/TentThis.properties' for schema writing!" );
			
			return;
		}
		
		//----------------------------------------------------------------------------------
		// Write the descriptive tags
		
		String nameTag = "<tentSchema=\"" + player.schemaName + "\">\n";
		String destructionTag = "<destructionBlock=" + player.destructionBlock + ">";
		String dimensionsTag = "<dimensions=" + length + "," + width + ">";
		String floorsTag = "<floors=" + height + ">";
		String colorTag = "<color=\"white\">";
		
		try 
		{
			writer.newLine( );
			
			writer.write( nameTag );
			writer.newLine( );
			
			writer.write( destructionTag );
			writer.newLine( );
			
			writer.write( dimensionsTag );
			writer.newLine( );
			
			writer.write( floorsTag );
			writer.newLine( );
			
			writer.write( colorTag );
			writer.newLine( );
		} 
		catch( IOException e ) 
		{
			log.info( "TentThis: Failed to write new schema! [DTS]" );
			
			e.printStackTrace();
			
			return;
		}
		
		//----------------------------------------------------------------------------------
		// Write to file
		
		log.info( "\n\nC: " + player.blockList.size( ) + "\nL: "+ length + "\nW: " + width + "\nH: " + height + "\n\n" );
		try
		{
			for( int y = 0; y < height; y++ )
			{
				writer.write( "<floor>" );
				writer.newLine( );
				
				for( int x = 0; x < width; x++ )
				{
					for( int z = 0; z < length; z++ )
					{
						//------------------------------------------------------------------
						// Ignore this block?
						
						if( player.cornerOne != null )
						{
							if( player.blockList.get( cntr ).equals( player.cornerOne ) )
							{
								player.cornerOne = null; 	//Stop checking this corner
								
								writer.write( "00." );
								
								cntr += 1;
								
								continue;
							}
						}
						
						if( player.cornerTwo != null )
						{
							if( player.blockList.get( cntr ).equals( player.cornerTwo ) )
							{
								player.cornerTwo = null; 	//Stop checking this corner
								
								writer.write( "00." );
								
								cntr += 1;
								
								continue;
							}
						}
						
						//------------------------------------------------------------------
						
						Block block;
						
						//Get the block

						block = player.blockList.get( cntr );
						
						log.info( "( " + block.getX( ) + ", " + block.getY( ) + ", " + block.getZ( ) + " )" );
						
						int ID = block.getTypeId( );
						
						//If door or bed
						if( ( ID == 64 ) || ( ID == 26 ) )
						{
							//Bed checks
							if( ID == 26 )
							{
								byte check = ( byte )( block.getData( ) & ~0x8 );
								
								if( block.getData( ) == check )
								{
									//Foot block
									writer.write( "BF." );
								}
								else
								{
									writer.write( "BH." );
								}
							}
							else
							{
								//Door check
								byte check = ( byte )( block.getData( ) & ~0x8 );
								
								if( block.getData( ) == check )
								{
									//Bottom
									writer.write( "DB." );
								}
								else
								{
									writer.write( "DT." );
								}
							}
						}
						else if( block.getTypeId( ) >= 10 )
						{
							//Normal blocks
							writer.write( block.getTypeId( ) + "." );
						}
						else
						{
							writer.write( "0" + block.getTypeId( ) + "." );
						}
						
						cntr += 1;
					}
					
					writer.newLine( );
				}
				
				writer.write( "</floor>" );
				writer.newLine( );
			}
		}
		catch( IOException e )
		{
			log.info( "TentThis: Failed to write new schema! [FLRS]" );
			
			e.printStackTrace();
		}
		
		try 
		{
			writer.write( "</tentSchema>" );
			writer.newLine( );
			
			writer.close( );
		} 
		catch( IOException e ) 
		{
			e.printStackTrace();
			
			return;
		}
	}
	
	//--------------------------------------------------------------------------------------
	
	public class ReversePlayer
	{
		String name;
		String schemaName;
		
		List< Block > blockList;
		
		int cornerToIgnore;
		int destructionBlock;
		
		Block cornerOne = null;
		Block cornerTwo = null;
		
		public ReversePlayer( String name, String schemaName, int cornerToIgnore, int destructionBlock )
		{
			this.name = name;
			this.schemaName = schemaName;
			this.cornerToIgnore = cornerToIgnore;
			this.destructionBlock = destructionBlock;
			
			blockList = new ArrayList< Block >( );
		}
	}
}
