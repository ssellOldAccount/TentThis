package ssell.TentThis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TentThis - Stupid Name, Cool Mod
 * 
 * @author Steven
 *
 */
public class TentThis
	extends JavaPlugin
{
	private static final Logger log = Logger.getLogger( "Minecraft" );
	
	private final TTSchemaLoader schemaLoader = new TTSchemaLoader( );
	
	private final TTBlockListener blockListener = new TTBlockListener( this, schemaLoader );
	
	private String schema = "default";
	
	private TTTent tent = schemaLoader.createTent( schema );
	
	//--------------------------------------------------------------------------------------
	
	public void onEnable( )
	{
		PluginManager pluginMgr = getServer( ).getPluginManager( );
		
		//Register for events
		pluginMgr.registerEvent( Event.Type.BLOCK_DAMAGED, blockListener, 
				                 Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.BLOCK_BREAK, blockListener, 
                Event.Priority.Normal, this );
		pluginMgr.registerEvent( Event.Type.BLOCK_RIGHTCLICKED, blockListener, 
                Event.Priority.Normal, this );
		
		getCreationBlock( );
		
		log.info( "TentThis v1.2.20 by ssell is enabled!" );		
	}
	
	public void onDisable( )
	{
		
	}
	
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		String[] split = args;
		String commandName = command.getName().toLowerCase();
	        
		if ( sender instanceof Player ) 
		{
			Player player = ( Player )sender;
			
			if( commandName.equals( "tttent" ) )
			{				
				//Listen for hit
				if( !blockListener.getListen( ) )
				{
					blockListener.setListen( true );
					player.sendMessage( ChatColor.GOLD + "Waiting to build tent..." );
				}
				else
				{
					blockListener.setListen( false );
					player.sendMessage( ChatColor.GOLD + "No longer waiting to build tent..." );
				}
				
				return true;
			}
			else if( commandName.equals( "ttschema" ) )
			{
				schema = split[ 0 ];
				
				if( schema != null )
				{
					tent = schemaLoader.createTent( schema );
					
					if( tent == null )
					{
						player.sendMessage( ChatColor.DARK_RED + "Failed to load in Tent '" + schema + "'!" );
					}
					else
					{
						player.sendMessage( ChatColor.GOLD + "Tent Schema '" + schema + "' loaded!" );
					}
				}
				else
				{
					player.sendMessage( ChatColor.DARK_RED + "Invalid Command. Use '/ttSchema <TentName>'" );
				}
				
				return true;
			}
			else if( commandName.equals( "ttwhat" ) )
			{
				player.sendMessage( ChatColor.GOLD + "The current schema is '" + schema + "'" );
				
				return true;
			}
			else if( commandName.equals( "ttnocommand" ) )
			{
				if( blockListener.getNoCommand( ) )
				{
					blockListener.setNoCommand( false );
					
					player.sendMessage( ChatColor.GOLD + "'NoCommand' disabled. Must enter '/ttTent' to build. Must left-click to build." );
				}
				else
				{
					blockListener.setNoCommand( true );
					blockListener.setListen( true );
					
					player.sendMessage( ChatColor.GOLD + "'NoCommand' enabled. No longer need to enter '/ttTent' to build. Must right-click to build." );
				}
				
				return true;
			}
			else if( commandName.equals( "ttreload" ) )
			{
				if( getCreationBlock( ) )
				{
					player.sendMessage( ChatColor.GOLD + "TentThis reload successful!" );
				}
				else
				{
					player.sendMessage( ChatColor.DARK_RED + "TentThis reload failed!" );
				}
				
				return true;
			}
		}
		
		return false;
	}
		
	public void buildTent( Player player, Block block )
	{
		schemaLoader.renderTent( player, block, tent );
	}
	
	public boolean getCreationBlock( )
	{
		Scanner scanner;
	
		try 
		{
			scanner = new Scanner( new BufferedReader( new FileReader( "TentThis.properties" ) ) );
		} 
		catch ( FileNotFoundException e ) 
		{
			log.info( "TentThis: Failed to find 'TentThis.properties'!" );
			
			return false;
		}
		
		while( scanner.hasNext( ) )
		{
			String string = scanner.next( );
			
			if( string.contains( "CreationBlock=" ) )
			{
				String substr = string.substring( 14 );
				
				try
				{
					int block = Integer.parseInt( substr.trim( ) );
					
					blockListener.creationBlock = block;
				}
				catch( NumberFormatException nfe )
				{
					log.info( "TentThis: '" + string + "' improperly formatted! [getCreationBlock]" );
					
					scanner.close( );
					
					return false;
				}
				
				scanner.close( );
				
				return true;
			}
		}
		
		scanner.close( );
		
		return false;
	}
}
