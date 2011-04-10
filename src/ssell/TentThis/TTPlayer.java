package ssell.TentThis;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;

public class TTPlayer 
{
	public TTTent currentTent;
	
	public List< JavaPair< String, List< Block > > > tentList = new ArrayList< JavaPair< String, List< Block > > >( );
	
	public int limit;
	
	public String name;
	
	public TTPlayer( )
	{
		
	}
	
	/**
	 * Removes tents if tentList.size( ) > limit.
	 */
	public void update( )
	{
		
	}
}
