package ssell.TentThis;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.inventory.ItemStack;

public class TTBlockListener 
	extends BlockListener
{
	private final TentThis plugin;
	private final TTSchemaLoader schemaLoader;
	
	private boolean listenForSponge = false;
	private boolean noCommand = false;
	private boolean destroying = false;
	
	public int creationBlock = 19;
	
	public TTBlockListener( TentThis instance, TTSchemaLoader schema )
	{
		plugin = instance;
		schemaLoader = schema;
	}
	
	@Override
	public void onBlockDamage( BlockDamageEvent event ) 
	{
		super.onBlockDamage( event );
		
		if( listenForSponge && !noCommand )
		{
			if( event.getBlock( ).getTypeId( ) == creationBlock )
			{
				plugin.buildTent( event.getPlayer( ), event.getBlock( ) );
				
				listenForSponge = false;
				
				return;
			}
		}
	}
	
	@Override
	public void onBlockRightClick( BlockRightClickEvent event )
	{
		super.onBlockRightClick( event );
		
		if( noCommand && listenForSponge )
		{
			if( event.getBlock( ).getType( ).equals( Material.SPONGE ) )
			{
				plugin.buildTent( event.getPlayer( ), event.getBlock( ) );
			}
		}
	}
	
	@Override
	public void onBlockBreak( BlockBreakEvent event )
	{
		super.onBlockBreak( event );
				
		if( event.getBlock( ).getType( ) == Material.WOOL )
		{
			List< Block > tent = schemaLoader.isBlockTent( event.getBlock( ) );
			
			if( tent != null )
			{
				event.getPlayer( ).getInventory( ).addItem( new ItemStack( creationBlock, 1 ) );
				
				schemaLoader.destroyTent( tent, event.getPlayer( ) );
			}
		}
		else if( invalidType( event.getBlock( ).getType( ) ) )
		{
			List< Block > tent = schemaLoader.isBlockTent( event.getBlock( ) );
			
			if( tent != null )
			{				
				event.setCancelled( true );
			}
		}
	}
	
	public boolean invalidType( Material material )
	{
		if( ( material == Material.WORKBENCH ) ||
			( material == Material.FURNACE ) ||
			( material == Material.TORCH ) ||
			( material == Material.WOOD_DOOR ) ||
			( material == Material.BED_BLOCK ) ||
			( material == Material.CHEST ) )
		{
			return true;
		}
		
		return false;
	}
	
	public void setListen( boolean set )
	{
		if( !noCommand )
		{
			listenForSponge = set;
		}
		else
		{
			listenForSponge = true;
		}
	}
	
	public void setNoCommand( boolean set )
	{
		noCommand = set;
	}
	
	public boolean getListen( )
	{
		return listenForSponge;
	}
	
	public boolean getNoCommand( )
	{
		return noCommand;
	}
}
