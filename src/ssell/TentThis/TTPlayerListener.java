package ssell.TentThis;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TTPlayerListener 
	extends PlayerListener
{
	private final TentThis plugin;
	
	public int creationBlock = 19;
	
	//The players that have noCommand enabled
	public List< String > listenList = new ArrayList< String >( );
	
	//--------------------------------------------------------------------------------------
	
	public TTPlayerListener( TentThis instance )
	{
		plugin = instance;
	}
	
	@Override
	public void onPlayerJoin( PlayerJoinEvent event )
	{
		super.onPlayerJoin( event );
		
		plugin.getServer( ).broadcastMessage( "Player joined.." );
		
		if( !plugin.manager.isTracked( event.getPlayer( ).getName( ) ) )
		{
			plugin.getServer( ).broadcastMessage( "They are new. Adding" );
			plugin.manager.addPlayer( event.getPlayer( ).getName( ) );
		}
	}
	
	@Override
	public void onPlayerInteract( PlayerInteractEvent event )
	{
		super.onPlayerInteract( event );
		
		//If right-click
		if( event.getAction( ).equals( Action.RIGHT_CLICK_BLOCK ) )
		{
			//Was on the creation block
			if( event.getClickedBlock( ).getTypeId( ) == creationBlock )
			{
				//Was it a player that we are listening for?
				if( listenList.contains( event.getPlayer( ).getName( ) ) )
				{
					plugin.buildTent( event.getPlayer( ).getName( ), event.getClickedBlock( ) );
				}
			}
		}
	}
	
	@Override
	public void onPlayerQuit( PlayerQuitEvent event )
	{
		super.onPlayerQuit( event );
		
		if( plugin.manager.isTracked( event.getPlayer( ).getName( ) ) )
		{
			plugin.manager.savePlayer( event.getPlayer( ).getName( ) );
		}
	}
}
