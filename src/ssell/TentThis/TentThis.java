package ssell.TentThis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class TentThis 
	extends JavaPlugin
{
	private static final Logger log = Logger.getLogger( "Minecraft" );
	
	public static PermissionHandler Permissions;
	public boolean permission = false;
	
	public final TTBlockListener blockListener = new TTBlockListener( this );
	public final TTPlayerListener playerListener = new TTPlayerListener( this );
	public final TTSchemaLoader schemaLoader = new TTSchemaLoader( this );
	public final TTManager manager = new TTManager( this );
	
	//--------------------------------------------------------------------------------------
	
	public void onEnable( )
	{
		PluginManager pluginMgr = getServer( ).getPluginManager( );
		
		pluginMgr.registerEvent( Event.Type.BLOCK_DAMAGE, blockListener, 
                Event.Priority.Low, this );
		pluginMgr.registerEvent( Event.Type.BLOCK_BREAK, blockListener, 
				Event.Priority.Low, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_INTERACT, playerListener, 
				Event.Priority.Low, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_JOIN, playerListener, 
				Event.Priority.Low, this );
		pluginMgr.registerEvent( Event.Type.PLAYER_QUIT, playerListener, 
				Event.Priority.Low, this );
		
		setupPermissions( );
		
		getCreationBlock( );
		getDefaultLimit( );
		setupSchemas( );
		
		log.info( "TentThis v2.1.0 by ssell is enabled!" );		
	}
	
	public void onDisable( )
	{
		manager.saveAll( );
	}
	
	/**
	 * Permissions List:<br><br>
	 * 
	 * TentThis.commands.setOwnSchema<br>
	 * TentThis.commands.setAllSchema<br>
	 * TentThis.commands.setLimit<br>
	 * TentThis.general.destroyAnyTent<br>
	 */
	private void setupPermissions( ) 
	{
	  	Plugin test = this.getServer( ).getPluginManager( ).getPlugin( "Permissions" );

	  	if( TentThis.Permissions == null ) 
	 	{
	     	if( test != null ) 
	     	{
	        	permission = true;
	        	TentThis.Permissions = ( ( Permissions )test ).getHandler( );
	      	} 
	    	else
	    	{
	        	log.info("Permission system not detected, defaulting to OP");
	     	}
	 	}
	}
	
	/**
	 * List of commands:<br><br>
	 *  /ttTent<br>
	 *  /ttSchema [Schema] [Player]<br>
	 *  /ttReload<br>
	 *  /ttLimit [Limit] [Player]<br>
	 */
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		String[] split = args;
		String commandName = command.getName().toLowerCase();
	        
		if ( sender instanceof Player ) 
		{
			Player player = ( Player )sender;
			
			if( commandName.equals( "tttent" ) )
			{				
				tentCommand( player );
				
				return true;
			}
			else if( commandName.equals( "ttschema" ) )
			{
				if( split.length == 2 )
				{
					schemaCommandAll( player, split[ 0 ], split[ 1 ] );
				}
				else if( split.length == 1 )
				{
					schemaCommandSelf( player, split[ 0 ] );
				}
				else
				{
					player.sendMessage( ChatColor.DARK_RED + "Improper use of command!" );
				}
				
				return true;
			}
			else if( commandName.equals( "ttlimit" ) )
			{
				if( split.length == 2 )
				{
					limitCommand( player, split[ 0 ], split[ 1 ] );
				}
				else
				{
					player.sendMessage( ChatColor.DARK_RED + "Improper use of command!" );
				}
				
				return true;
			}
			else if( commandName.equals( "ttnocommand" ) )
			{
				noCommand( player );
				
				return true;
			}
			else if( commandName.equals( "ttreload" ) )
			{
				reloadCommand( player );
				
				return true;
			}
			else if( commandName.equals( "ttinfo" ) )
			{
				infoCommand( player );
				
				return true;
			}
		}
		
		return false;
	}
	
	//--------------------------------------------------------------------------------------
	// Command Methods
	
	/**
	 * Called when player uses /ttTent command.
	 */
	public void tentCommand( Player player )
	{
		if( blockListener.listenList.contains( player.getName( ) ) )
		{
			//Already on. Don't wait anymore
			blockListener.listenList.remove( player.getName( ) );
			
			player.sendMessage( ChatColor.GOLD + "No longer waiting to build tent." );
		}
		else
		{
			blockListener.listenList.add( player.getName( ) );
			
			//Remove player from the playerListener list (NoCommand)
			if( playerListener.listenList.contains( player.getName( ) ) )
			{
				playerListener.listenList.remove( player.getName( ) );
				player.sendMessage( ChatColor.GOLD + "NoCommand disabled!" );
			}
			
			player.sendMessage( ChatColor.GOLD + "Waiting to build tent." );
		}
	}
	
	/**
	 * Called when a player attempts to set their own schema.
	 * 
	 * @param player
	 * @param schema
	 */
	public void schemaCommandSelf( Player player, String schema )
	{
		//Using Permissions?
		if( permission )
		{
			//Player has permission?
			if( !TentThis.Permissions.has( player, "TentThis.commands.setOwnSchema" ) )
			{
				player.sendMessage( ChatColor.DARK_RED + "You don't have permission to perform this action!" );
			}
		}
		
		//Did the schema setting work?
		if( manager.setSchema( schema, player.getName( ) ) )
		{
			player.sendMessage( ChatColor.GOLD + "'" + schema + "' set as schema!" );
		}	
		else
		{
			player.sendMessage( ChatColor.DARK_RED + "Failed to set '" + schema + "' as schema!" );
		}
	}
	
	/**
	 * Called when a player attempts to set the schema of another.<br><br>
	 * If '-all' is provided for who, then it is set to all players.
	 * 
	 * @param player
	 * @param schema
	 * @param who
	 */
	public void schemaCommandAll( Player player, String schema, String who )
	{
		//Using Permissions?
		if( permission )
		{
			//Player has permission?
			if( !TentThis.Permissions.has( player, "TentThis.commands.setAllSchema" ) )
			{
				player.sendMessage( ChatColor.DARK_RED + "You don't have permission to perform this action!" );
			}
		}
		
		//Did the schema setting work?
		if( manager.setSchema( schema, who ) )
		{
			player.sendMessage( ChatColor.GOLD + "'" + schema + "' set as schema for '" + who +"'!" );
		}
		else
		{
			player.sendMessage( ChatColor.DARK_RED + "Failed to set '" + schema + "' as schema!" );
		}
	}
	
	/**
	 * Sets the tent limit for the specified player.<br><br>
	 * If '-all' is provided as the player, then it is set for everyone.
	 * 
	 * @param player
	 * @param name
	 */
	public void limitCommand( Player player, String name, String limitStr )
	{
		log.info( "ttLimit: " + name + " " + limitStr );
		//Using Permissions?
		if( permission )
		{
			if( !TentThis.Permissions.has( player, "TentThis.commands.setLimit" ) )
			{
				player.sendMessage( ChatColor.DARK_RED + "You don't have permission to perform this action!" );
				
				return;
			}
		}	
		
		int limit;
			
		//Make sure the user provided an integer
		try
		{
			limit = Integer.parseInt( limitStr );
		}
		catch( NumberFormatException nfe )
		{
			player.sendMessage( ChatColor.DARK_RED + "Integer value provided is not an integer!" );
				
			return;
		}
			
		manager.setLimit( limit, name );
			
		player.sendMessage( ChatColor.GOLD + "Limit set to " + limit + " for '" + name + "'!" );
	}
	
	/**
	 * Handles /ttNoCommand.<br><br>
	 * If enabled, player no longer needs to call /ttTent and needs to right-click the block.
	 * 
	 * @param player
	 */
	public void noCommand( Player player )
	{
		if( playerListener.listenList.contains( player.getName( ) ) )
		{
			//Already on the list. Remove them.
			playerListener.listenList.remove( player.getName( ) );
			
			player.sendMessage( ChatColor.GOLD + "No longer using NoCommand! Must manually use /ttTent and left-click blocks." );
		}
		else
		{
			playerListener.listenList.add( player.getName( ) );
			
			player.sendMessage( ChatColor.GOLD + "Now using NoCommand! Must right-click blocks in this mode." );
		}
		
		if( blockListener.listenList.contains( player.getName( ) ) )
		{
			blockListener.listenList.remove( player.getName( ) );
		}
	}
	
	/**
	 * Updates the creation block.
	 * 
	 * @param player
	 */
	public void reloadCommand( Player player )
	{
		if( getCreationBlock( ) )
		{
			player.sendMessage( ChatColor.GOLD + "TentThis reload successful!" );
		}
		else
		{
			player.sendMessage( ChatColor.DARK_RED + "TentThis reload failed!" );
		}
	}
	
	/**
	 * Lists the following to the player:<br><br>
	 * CreationBlock<br>
	 * # of Tents / Tent Limit<br>
	 * List of schemas. Current is green.
	 * 
	 * @param player
	 */
	public void infoCommand( Player player )
	{
		//Example: CreationBlock: Sponge [19]
		player.sendMessage( ChatColor.GOLD + "CreationBlock: " + 
				Material.getMaterial( blockListener.creationBlock ) + 
				" [" + blockListener.creationBlock + "]" );
		
		TTPlayer ttPlayer = manager.getPlayer( player.getName( ) );
		
		//Example: TentLimit: 3/16
		if( ( ttPlayer.tentList.size( ) < ttPlayer.limit ) ||
			( ttPlayer.limit < 0 ) )
		{
			player.sendMessage( ChatColor.GOLD + "TentLimit: " + ChatColor.GREEN +
					ttPlayer.tentList.size( ) + "/" + ttPlayer.limit );
		}
		else
		{
			player.sendMessage( ChatColor.GOLD + "TentLimit: " + ChatColor.RED +
					ttPlayer.tentList.size( ) + "/" + ttPlayer.limit );
		}
		
		player.sendMessage( ChatColor.GOLD + "List of Schemas:" );
		
		for( int i = 0; i < manager.tentList.size( ); i++ )
		{
			if( ttPlayer.currentTent.equals( manager.tentList.get( i ) ) )
			{
				player.sendMessage( ChatColor.GREEN + manager.tentList.get( i ).schemaName );
			}
			else
			{
				player.sendMessage( ChatColor.RED + manager.tentList.get( i ).schemaName );
			}
		}
		
	}
	
	//--------------------------------------------------------------------------------------
	
	public void buildTent( String name, Block block )
	{
		TTPlayer player = manager.getPlayer( name );
		
		if( player != null )
		{
			if( ( player.tentList.size( ) < player.limit ) || ( player.limit < 0 ) ) 
			{
				//Player can build more
				schemaLoader.renderTent( getServer( ).getPlayer( name ), block, manager.getPlayer( name ).currentTent );
				
			}
			else
			{
				getServer( ).getPlayer( name ).sendMessage( ChatColor.DARK_RED + "You are at your limit! Destroy an existing tent." );
			}
		}
	}
	
	public boolean getCreationBlock( )
	{
		Scanner scanner;
	
		try 
		{
			scanner = new Scanner( new BufferedReader( new FileReader( "plugins/TentThis/TentThis.properties" ) ) );
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
					playerListener.creationBlock = block;
					
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
			else if( string.contains( "TentLimit=" ) )
			{
				String substr = string.substring( 14 );
				
				try
				{
					int limit = Integer.parseInt( substr.trim( ) );
					
					manager.globalLimit = limit;
					
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
	
	public boolean getDefaultLimit( )
	{
		Scanner scanner;
	
		try 
		{
			scanner = new Scanner( new BufferedReader( new FileReader( "plugins/TentThis/TentThis.properties" ) ) );
		} 
		catch ( FileNotFoundException e ) 
		{
			log.info( "TentThis: Failed to find 'TentThis.properties'!" );
			
			return false;
		}
		
		while( scanner.hasNext( ) )
		{
			String string = scanner.next( );
			
			if( string.contains( "TentLimit=" ) )
			{
				String substr = string.substring( 10 );
				
				try
				{
					int limit = Integer.parseInt( substr.trim( ) );
					
					manager.globalLimit = limit;
					
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
	
	public void setupSchemas( )
	{		
		//Gather all of the schemas
		Scanner scanner;
		
		try 
		{
			scanner = new Scanner( new FileReader( "plugins/TentThis/TentThis.properties" ) );
		} 
		catch( FileNotFoundException e ) 
		{
			log.info( "TentThis: Failed to open TentThis.properties! [SetupSchemas]" );
			return;
		}
		
		while( scanner.hasNext( ) )
		{
			String str = scanner.next( );
			
			if( str.contains( "<tentSchema=") )
			{
				String name = str.substring( 13, str.length( )- 2 );
				
				manager.createTent( name );
			}
		}
		
		//Set default as the first tent in the list
		manager.defaultSchema = manager.tentList.get( 0 ).schemaName;
	}
}
