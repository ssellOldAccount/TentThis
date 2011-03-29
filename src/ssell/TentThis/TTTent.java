package ssell.TentThis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class TTTent 
{
	public int length = 0;
	public int width = 0;
	public int height = 0;
	public int color = 0;
	
	public Location origin;
	
	public String schemaName;
	    
	       //Y      Z   X           
	public List< List< List< Material > > > blockList = new ArrayList< List< List< Material > > >( );
	
	public TTTent( String name, int length, int width, int height, String color )
	{
		schemaName = name;
		this.length = length;
		this.width = width;
		this.height = height;
		
		//Get the color
		if( color != null )
		{
			if( color.contains( "white" ) )
			{
				this.color = 0;
			}
			else if( color.contains( "orange" ) )
			{
				this.color = 1;
			}
			else if( color.contains( "magenta" ) )
			{
				this.color = 2;
			}
			else if( color.contains( "lightblue" ) )
			{
				this.color = 3;
			}
			else if( color.contains( "yellow" ) )
			{
				this.color = 4;
			}
			else if( color.contains( "limegreen" ) )
			{
				this.color = 5;
			}
			else if( color.contains( "pink" ) )
			{
				this.color = 6;
			}
			else if( color.contains( "gray" ) )
			{
				this.color = 7;
			}
			else if( color.contains( "lightgray" ) )
			{
				this.color = 8;
			}
			else if( color.contains( "cyan" ) )
			{
				this.color = 9;
			}
			else if( color.contains( "purple" ) )
			{
				this.color = 10;
			}
			else if( color.contains( "blue" ) )
			{
				this.color = 11;
			}
			else if( color.contains( "brown" ) )
			{
				this.color = 12;
			}
			else if( color.contains( "green" ) )
			{
				this.color = 13;
			}
			else if( color.contains( "red" ) )
			{
				this.color = 14;
			}
			else if( color.contains( "black" ) )
			{
				this.color = 15;
			}
		}
	}
	
}
