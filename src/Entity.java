import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Entity {
	private static final int AUTO_MOVE_SPEED = 2;
	
	private int x;
	private int y;
	private int dx;
	private int dy;
	
	// width/height
	private int w;
	private int h;
	
	private World world;
		
	private Point[] path;
	private int pathLen;
	private int currentTarget;
	private boolean autoMove;
	
	private boolean hasCollided;
	
	private Point target;
	private Point pTarget;
	

	public Entity(World world, int x, int y) {
		this.x = x;
		this.y = y;
		this.w = world.blockSize;
		this.h = world.blockSize;
		this.world = world;
		
		this.hasCollided = false;
	}
	
	public void draw(Graphics2D g) {
		g.setColor(Color.BLUE);

//		pathfind debug code
//
//		for (int i = 0; i < pathLen; i++) {
//			Point p = path[i];
//			g.fillRect(p.x, p.y, world.blockSize, world.blockSize);
//		}
		
		g.setColor(Color.RED);
		g.fillOval(x - w/2, y - h/2, w, h);
	}
	
	public void update() {
		if (autoMove) 
			autoMove = autoMove();
		
		y += dy;
		x += dx;
	}
	
	public void setTarget(Point target) {
		if (!target.equals(this.pTarget)) {
			this.pTarget = target;
			this.target = new Point(target.x/world.blockSize, target.y/world.blockSize);
			
			// Convert the path to an array
			path = pathFind().toArray(new Point[0]);
			pathLen = path.length;
			currentTarget = 0;
			autoMove = true;
		}
	}
	
	// Set the velocity of the entity to be what it needs to be to get to the next
	// point in path
	private boolean autoMove() {
		Point target = path[currentTarget];
		
		// We have collided with something so the path-finding is over
		if (hasCollided && dx == 0 && dy == 0)
			return false;
		
		if (currentTarget == pathLen - 1)
			target = pTarget;
		
		if (target.x != x || target.y != y) {
			int xDist = target.x - x;
			int yDist = target.y - y;
							 
			if (yDist > 0) {
				dy = Math.min(AUTO_MOVE_SPEED, yDist);
			} else if (yDist < 0) {
				dy = Math.max(-AUTO_MOVE_SPEED, yDist);
			} else {
				dy = 0;
			}
			
			if (xDist > 0) {
				dx = Math.min(AUTO_MOVE_SPEED, xDist);
			} else if (xDist < 0) {
				dx = Math.max(-AUTO_MOVE_SPEED, xDist);
			} else {
				dx = 0;
			}
		} else {
			dx = 0; 
			dy = 0;
			
			if (++currentTarget >= pathLen) {
				return false;
			}
		}
		
		return true;
	}
	
	// Use A* to calculate a new path for the entity. Stores it in path variable.
	private ArrayList<Point> pathFind() {
		int gx = x/world.blockSize;
		int gy = y/world.blockSize;
		
		APointList openList = new APointList();
		APointList closedList = new APointList();
		APoint current = new APoint(target, gx, gy);
		closedList.add(current);
				
		while (!current.equals(target)) {
			//System.out.println(current);
			openList.addSurrounding(world, current, closedList, target);
			current = openList.getLowest();
			openList.remove(current);
			closedList.n_add(current);
		}
		
		ArrayList<Point> f = new ArrayList<Point>();
		
		APoint p = current;
		APoint d = null; // last point
		
		// Stores all the points such that only changes in direction are saved
		while (!p.equals(new Point(gx, gy))) {
			if (d != null)  {
				Point g = p.getParent();
				
				// Check to see if there is a turn
				if ((d.x == p.x && p.x != g.x) ||
					(d.y == p.y && p.y != g.y)) {
					f.add(0, p.getAdjusted(world.blockSize));
				}
			} else {
				f.add(0, p.getAdjusted(world.blockSize));
			}
			
			d = p;
			p = p.getParent();
		}
				
		return f;
	}
}
