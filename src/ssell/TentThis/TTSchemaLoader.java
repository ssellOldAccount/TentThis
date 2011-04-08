package ssell.TentThis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class TTSchemaLoader 
{
	private static final Logger log = Logger.getLogger( "Minecraft" );
	
	private final TentThis plugin;
	
	//--------------------------------------------------------------------------------------
	
	public TTSchemaLoader( TentThis instance )
	{
		plugin = instance;
	}
	
	public TTTent createTent( String name )
	{		
		Scanner scanner;
		
		try 
		{
			scanner = new Scanner( new BufferedReader( new FileReader( "plugins/TentThis/TentThis.properties" ) ) );
		} 
		catch ( FileNotFoundException e ) 
		{
			log.info( "TentThis: Failed to find '/plugins/TentThis/TentThis.properties'!" );
			
			return null;
		}

		boolean properSchema = false;
		
		int l, w, h;
		
		//----------------------------------------------------------------------------------
		// Does the schema exist?
		
		while( scanner.hasNext( ) )
		{
			String findName = scanner.next( );
			
			if( findName.contains( "<tentSchema=" ) )
			{
				//Proper line.
				if( findName.contains( name ) )
				{
					properSchema = true;
					break;
				}
			}
		}
		
		if( !properSchema )
		{
			log.info( "TentThis: Schema by the name of '" + name + "' does not exist! [TTSchemaLoader]" );
			
			scanner.close( );
			
			return null;
		}
		
		//----------------------------------------------------------------------------------
		// Schema does exist. Start grabbing basic info and make sure properly formatted.
		
		TTTent tent;
		
		//The scanner should be in the proper place due to the break
		
		//Get length and width dimensions
		if( scanner.hasNext( ) )
		{
			String string = scanner.next( );
			
			if( string.contains( "<dimensions=" ) )
			{				
				String width = string.substring( string.indexOf( '=' ) + 1, string.indexOf( ',' ) );
				String length = string.substring( string.indexOf( ',' ) + 1, string.indexOf( '>' ) );
				
				try
				{
					l = Integer.parseInt( length.trim( ) );
				}
				catch( NumberFormatException nfe )
				{
					log.info( "TentThis: Could not parse length from '" + name + "'! [TTSchemaLoader]" );
					
					scanner.close( );
					
					return null;
				}
				
				try
				{
					w = Integer.parseInt( width.trim( ) );
				}
				catch( NumberFormatException nfe )
				{
					log.info( "TentThis: Could not parse width from '" + name + "'! [TTSchemaLoader]" );
					
					scanner.close( );
					
					return null;
				}
			}
			else
			{
				log.info( "TentThis: No <dimensions=#,#> tag in '" + name + "'! [TTSchemaLoader]" );
				
				scanner.close( );
				
				return null;
			}
		}
		else
		{
			log.info( "TentThis: Unexpected end to schema file! [TTSchemaLoader]" );
			
			return null;
		}
		
		//Get height
		if( scanner.hasNext( ) )
		{
			String string = scanner.next( );
			
			if( string.contains( "<floors=" ) )
			{
				String height = string.substring( string.indexOf( '=' ) + 1, string.indexOf( '>' ) );
				
				try
				{
					h = Integer.parseInt( height.trim( ) );
				}
				catch( NumberFormatException nfe )
				{
					log.info( "TentThis: Could not parse number of floors from '" + name + "'! [TTSchemaLoader]" );
					
					scanner.close( );
					
					return null;
				}
			}
			else
			{
				log.info( "TentThis: No <floors=#> tag in '" + name + "'! [TTSchemaLoader]" );
				
				scanner.close( );
				
				return null;
			}
		}
		else
		{
			log.info( "TentThis: Unexpected end to schema file! [TTSchemaLoader]" );
			
			scanner.close( );
			
			return null;
		}
		
		//----------------------------------------------------------------------------------
		//Get Color
		
		String color = null;
		
		if( scanner.hasNext( ) )
		{
			String string = scanner.next( );
			
			if( string.contains( "<color=" ) )
			{
				color = string;
			}
			else
			{
				log.info( "TentThis: Expected <color=COLOR> but got " + string + "! [TTSchemaLoader]" );
			}
		}
		else
		{
			log.info( "TentThis: Unexpected end to schema file! [TTSchemaLoader]" );
			
			scanner.close( );
			
			return null;
		}
		
		//Create the tent. We have all required beginning information
		tent = new TTTent( name, l, w, h, color );
	
		
		//----------------------------------------------------------------------------------
		// Set the materials. Can not set locations until it is called to make the tent
		
		List< List< List< Material > > > completeList = new ArrayList< List< List< Material > > >( );
		List< List< Material > > xzList = new ArrayList< List< Material > >( );
		List< Material > xList = new ArrayList< Material >( );
		
		while( scanner.hasNext( ) )
		{			
			String string = scanner.next( );
			
			if( string.equalsIgnoreCase( "<tentSchema>" ) || string.contains( "dimensions" ) )
			{
				break;
			}
			
			while( !string.contains( "tentSchema" ) )
			{
				if( string.equalsIgnoreCase( "</floor>" ) )
				{
					string = scanner.next( );
					
					if( !xzList.isEmpty( ) )
					{
						completeList.add( xzList );
				
						xzList = new ArrayList< List< Material > >( );
					}
				}
				
			    if( string.equalsIgnoreCase( "<floor>" ) )
				{
					string = scanner.next( );
				}
				
				while( !string.equalsIgnoreCase( "</floor>" ) && !string.equalsIgnoreCase( "<tentSchema>" ) )
				{
					if( string.contains( "tentSchema" ) )
					{
						break;
					}
					
					if( string.length( ) < w )
					{
						log.info( "TentThis: '" + string + "' is shorter than the specified width of " +
								w + "! [TTSchemeLoader]" );
						
						scanner.close( );
						
						return null;
					}
					
					for( int i = 0; i < w; i++ )
					{
						char c = string.charAt( i );
						
						switch( c )
						{
						case '_':
							//Air
							xList.add( Material.AIR );
							break;
						case 'W':
							//Wall (Wool)
							xList.add( Material.WOOL );
							break;
						case 'B':
							//Bed
							xList.add( Material.JUKEBOX );
							break;
						case 'H':
							xList.add( Material.SPONGE );
							break;
						case 'F':
							//Furnace
							xList.add( Material.FURNACE );
							break;
						case 'C':
							//Chest
							xList.add( Material.CHEST );
							break;
						case 'T':
							//Crafting Table
							xList.add( Material.WORKBENCH );
							break;
						case 'L':
							//Light Source (Torch)
							xList.add( Material.TORCH );
							break;
						case 'D':
							//Door
							xList.add( Material.JACK_O_LANTERN );
							break;
						default:
							log.info( "TentThis: Invalid Character '" + c + "' in line '" +
									string + "' in schema '" + 
									name + "'! [TTSchemaLoader]" );
							
							scanner.close( );
							
							return null;
						}
					}
					
					string = scanner.next( );
					
					if( !xList.isEmpty( ) )
					{
						xzList.add( xList );
						
						xList = new ArrayList< Material >( );
					}
				}
			}
		}
		
		tent.blockList = completeList;
				
		scanner.close( );
		
		return tent;
	}
	
	public boolean renderTent( Player player, Block block, TTTent tent )
	{
		Location origin = block.getLocation( );
		
		int alignment = 0;
		
		if( Math.floor( player.getLocation( ).getZ( ) ) < Math.floor( origin.getZ( ) ) )
		{
			alignment = 1;
		}
		else if( Math.floor( player.getLocation( ).getZ( ) ) > Math.floor( origin.getZ( ) ) )
		{
			alignment = 2;
		}
		else if( Math.floor( player.getLocation( ).getX( ) ) < Math.floor( origin.getX( ) ) )
		{
			alignment = 3;
		}
		else
		{
			alignment = 4;
		}
		
		//----------------------------------------------------------------------------------
		
		if( !isThereSpace( origin, alignment, tent, player ) )
		{
			player.sendMessage( ChatColor.DARK_RED + "There is not enough space to build Tent '" +
					tent.schemaName + "'!" );
			
			return false;
		}
		
		//----------------------------------------------------------------------------------
		
		return buildTent( origin, alignment, tent, player );
	}
	
	public boolean isThereSpace( Location location, int alignment, TTTent tent, Player player )
	{
		if( tent != null )
		{
			int length = tent.length;
			int width = tent.width;
			int height = tent.height;
			
			for( int y = 0; y < height; y++ )
			{
				for( int z = 0; z < length; z++ )
				{
					for( int x = 0; x < width; x++ )
					{
						Location check;
						
						if( ( x + y + z ) == 0 )
						{
							continue;
						}
							
						if( alignment == 1 )
						{
							check = new Location( player.getWorld( ),
												  location.getX( ) - x, 
										          location.getY( ) + y, 
										          location.getZ( ) + z );
						}
						else if( alignment == 3 )
						{
							check = new Location( player.getWorld( ),
										          location.getX( ) + x, 
							                      location.getY( ) + y, 
							                      location.getZ( ) + z );
						}
						else if( alignment == 2 )
						{
							check = new Location( player.getWorld( ),
							          location.getX( ) + z, 
				                      location.getY( ) + y, 
				                      location.getZ( ) - x );
						}
						else
						{
							check = new Location( player.getWorld( ),
							          location.getX( ) - z, 
				                      location.getY( ) + y, 
				                      location.getZ( ) - x );
						}
							
						if( player.getWorld( ).getBlockAt( check ).getType( ) != Material.AIR )
						{
							return false;
						}
					}
				}
			}
		}
		else
		{
			log.info( "TentThis: NULL tent passed to 'isThereSpace'!" );
		}
		
		return true;
	}
	
	//--------------------------------------------------------------------------------------
	
	public boolean buildTent( Location location, int alignment, TTTent tent, Player player )
	{
		int length = tent.blockList.get( 0 ).size( );
		int width = tent.blockList.get( 0 ).get( 0 ).size( );
		int height = tent.blockList.size( );
		
		player.sendMessage( ChatColor.GOLD + "Building Tent '" + tent.schemaName + "'!" );
		
		List< Block > newTent = new ArrayList< Block >( );
		
		for( int y = 0; y < height; y++ )
		{
			for( int z = 0; z < length; z++ )
			{
				for( int x = 0; x < width; x++ )
				{
					Location check;
						
					if( alignment == 1 )
					{
						check = new Location( player.getWorld( ),
											  location.getX( ) - x, 
									          location.getY( ) + y, 
									          location.getZ( ) + z );
					}
					else if( alignment == 3 )
					{
						check = new Location( player.getWorld( ),
									          location.getX( ) + x, 
						                      location.getY( ) + y, 
						                      location.getZ( ) + z );
					}
					else if( alignment == 2 )
					{
						check = new Location( player.getWorld( ),
						          location.getX( ) + z, 
			                      location.getY( ) + y, 
			                      location.getZ( ) - x );
					}
					else
					{
						check = new Location( player.getWorld( ),
						          location.getX( ) - z, 
			                      location.getY( ) + y, 
			                      location.getZ( ) - x );
					}
					
					Material material = tent.blockList.get( y ).get( z ).get( x );
					
					if( material != null )
					{						
						if( material == Material.BED_BLOCK )
						{
							player.getWorld( ).getBlockAt( check ).setTypeIdAndData( 26, ( byte )0, false );
						}
						else
						{
							player.getWorld( ).getBlockAt( check ).setType( material );
						}
						
						if( material == Material.WOOL )
						{
							player.getWorld( ).getBlockAt( check ).setData( ( byte )tent.color );
						}
							
						newTent.add( player.getWorld( ).getBlockAt( check ) );
					}
				}
			}
		}
		
		bedsAndDoors( newTent, alignment );
		
		TTPlayer ttPlayer = plugin.manager.getPlayer( player.getName( ) );
		
		if( ttPlayer != null )
		{
			ttPlayer.tentList.add( newTent );
		}
		else
		{
			log.info( "TentThis: TTPlayer is null! [TTSchemaLoader|RenderTent]" );
		}
		
		return true;
	}
	
	public void bedsAndDoors( List< Block > list, int alignment )
	{
		//Make the beds and doors work
		//Beds
		for( int i = 0; i < list.size( ); i++ )
		{
			Block block = list.get( i );
			
			if( block.getType(  ) == Material.JUKEBOX ) 
			{
			    for( BlockFace face: BlockFace.values( ) ) 
			    {
			    	if( face != BlockFace.DOWN && face != BlockFace.UP ) 
			    	{
			    		final Block facingBlock = block.getFace( face );
			        
			    		if( facingBlock.getType( ) == Material.SPONGE ) 
			    		{
			    			byte flags = ( byte )8;
			    			byte direction = ( byte )( 0x0 );
			          
			    			switch( face ) 
			    			{

			    	          case EAST:  
			    	        	  flags = ( byte )( flags | 0x2 ); 
			    	        	  direction = ( byte )( 0x2 );
			    	        	  break;

			    	          case SOUTH: 
			    	        	  flags = ( byte )( flags | 0x3 );  
			    	        	  direction = ( byte )( 0x3 );
			    	        	  break;

			    	          case WEST:  
			    	        	  flags = ( byte )( flags | 0x0 );  
			    	        	  direction = ( byte )( 0x0 );
			    	        	  break;

			    	          case NORTH: 
			    	        	  flags = ( byte )( flags | 0x1 );  
			    	        	  direction = ( byte )( 0x1 );
			    	        	  break;
			    	          }

			    			facingBlock.setType( Material.AIR );
				    		block.setTypeIdAndData( 26, direction, true );
				    		facingBlock.setTypeIdAndData( 26, flags, true );	 
			    		}	
			    	}
			    }
			}
			else if( block.getType( ).equals( Material.JACK_O_LANTERN ) )
			{
				Location loc = block.getLocation( );
				
				Block check = block.getWorld( ).getBlockAt( 
									new Location( list.get( i ).getWorld( ),
								        loc.getX( ) - 1,
								        loc.getY( ),
								        loc.getZ( ) ) );
				
				byte side = 4;
				
				//Find a wall
				//Is it to the north?
				if( check.getType( ).equals( Material.WOOL ) )
				{
					side = 1;
				}
				
				if( side == 4 )
				{
					check = list.get( i ).getWorld( ).getBlockAt( 
							new Location( list.get( i ).getWorld( ),
						        loc.getX( ) + 1,
						        loc.getY( ),
						        loc.getZ( ) ) );
					
					//South
					if( check.getType( ).equals( Material.WOOL ) )
					{
						side = 2;
					}
				}
				
				if( side == 4 )
				{
					check = list.get( i ).getWorld( ).getBlockAt( 
							new Location( list.get( i ).getWorld( ),
						        loc.getX( ),
						        loc.getY( ),
						        loc.getZ( ) + 1 ) );
					
					//South
					if( check.getType( ).equals( Material.WOOL ) )
					{
						side = 0;
					}
				}
				
				if( side == 4 )
				{
					side = 3;
				}
								
				list.get( i ).setTypeIdAndData( 64, side, false);
				
				list.get( i ).getFace( BlockFace.UP ).setTypeIdAndData(64, ( byte ) 8, true );
			}
		}
	}
	/*
	public List< Block > isBlockTent( Block block )
	{
		for( int i = 0; i < tentList.size( ); i++ )
		{
			for( int j = 0; j < tentList.get( i ).size( ); j++ )
			{
				if( tentList.get( i ).get( j ).equals( block ) )
				{
					//part of a tent
					return tentList.get( i );
				}
			}
		}
		return null;
	}*/
	
	public void destroyTent( List< Block > tent, Player player )
	{		
		for( int i = 0; i < tent.size( ); i++ )
		{
			if( tent.get( i ).getType( ).equals( Material.TORCH ) )
			{
				tent.get( i ).setType( Material.AIR );
			}
			else if( tent.get( i ).getType( ).equals( Material.BED_BLOCK ) )
			{
				//Destroy the foot before the head or else it will drop a bed
				
				if( !( tent.get( i ).getData( ) == ( ( byte )0 | 0x8 ) ) &&
					!( tent.get( i ).getData( ) == ( ( byte )1 | 0x8 ) ) &&
					!( tent.get( i ).getData( ) == ( ( byte )2 | 0x8 ) ) &&
					!( tent.get( i ).getData( ) == ( ( byte )3 | 0x8 ) ) )
				{
					tent.get( i ).setType( Material.AIR );
				}
			}
		}
		
		for( int i = 0; i < tent.size( ); i++ )
		{
			tent.get( i ).setType( Material.AIR );
		}
		
		tent.clear( );
		
		TTPlayer ttPlayer = plugin.manager.getPlayer( player.getName( ) );
		
		if( ttPlayer != null )
		{
			ttPlayer.tentList.remove( tent );
		}
		else
		{
			log.info( "TentThis: TTPlayer is null! [SchemaLoader|DestroyTent]" );
		}
	}
}
