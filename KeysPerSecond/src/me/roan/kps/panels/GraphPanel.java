package me.roan.kps.panels;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.LinkedList;

import javax.swing.JPanel;

import me.roan.kps.ColorManager;
import me.roan.kps.Main;
import me.roan.kps.SizeManager;
import me.roan.kps.layout.LayoutPosition;

/**
 * Panel to draw continuous graphs
 * @author Roan
 */
public class GraphPanel extends JPanel implements LayoutPosition{
	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 1805853149241193714L;
	/**
	 * Data points
	 */
	private LinkedList<Integer> values = new LinkedList<Integer>();
	/**
	 * Highest encountered value used as the
	 * upper bound of the graph
	 */
	private int maxval = 1;
	/**
	 * Stroke used to draw the graph
	 */
	private static final Stroke line = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0F, null, 0);
	/**
	 * Stroke used to draw average line
	 */
	private static final Stroke avgstroke = new BasicStroke(1.0F, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0F, null, 0);

	/**
	 * Resets the graph
	 */
	public final void reset(){
		values.clear();
		maxval = 1;
	}

	@Override
	public void paintComponent(Graphics g1){
		if(Main.config.showGraph){
			try{
				Graphics2D g = (Graphics2D)g1;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				if(ColorManager.transparency){
					g.setColor(ColorManager.transparent);
					g.fillRect(0, 0, this.getWidth(), this.getHeight());
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Main.config.getBackgroundOpacity()));
					g.setColor(Main.config.getBackgroundColor());
					g.fillRect(0, 0, this.getWidth(), this.getHeight());
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Main.config.getBackgroundOpacity()));
				}else{
					g.setColor(Main.config.getBackgroundColor());
					g.fillRect(0, 0, this.getWidth(), this.getHeight());
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
				}
				Polygon poly = new Polygon();
				poly.addPoint(this.getWidth() - SizeManager.insideOffset - 2, this.getHeight() - SizeManager.insideOffset - 1);
				for(int i = 1; i <= values.size(); i++){
					int px = (int)(SizeManager.insideOffset + ((double)(this.getWidth() - SizeManager.insideOffset * 2 - 2) / (double)(Main.config.backlog - 1)) * (Main.config.backlog - i));
					int py = (int)(this.getHeight() - SizeManager.insideOffset - 1 - ((float)(this.getHeight() - SizeManager.insideOffset * 2) * ((float)values.get(i - 1) / (float)maxval)));
					poly.addPoint(px, py);
					if(i == values.size()){
						poly.addPoint(px, this.getHeight() - SizeManager.insideOffset - 1);
					}
				}
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Main.config.getForegroundOpacity()));
				if(Main.config.graphAvg){
					int y = (int)(this.getHeight() - SizeManager.insideOffset - ((float)(this.getHeight() - SizeManager.insideOffset * 2) * (Main.avg / (float)maxval)));
					g.setColor(Main.config.getForegroundColor().darker());
					g.setStroke(avgstroke);
					g.drawLine(SizeManager.insideOffset, y, this.getWidth() - SizeManager.insideOffset - 2, y);
				}
				g.setStroke(line);
				g.setColor(ColorManager.alphaAqua);
				g.fillPolygon(poly);
				g.setColor(Main.config.getForegroundColor());
				g.drawPolygon(poly);
				g.drawImage(ColorManager.graph_upper_left,   2, 2, 2 + SizeManager.graphImageSize, 2 + SizeManager.graphImageSize, 0, 0, 4, 4, this);
				g.drawImage(ColorManager.graph_lower_left,   2, this.getHeight() - 3 - SizeManager.graphImageSize, 2 + SizeManager.graphImageSize, this.getHeight() - 3, 0, 0, 4, 4, this);
				g.drawImage(ColorManager.graph_upper_right,  this.getWidth() - 3 - SizeManager.graphImageSize, 2, this.getWidth() - 3, 2 + SizeManager.graphImageSize, 0, 0, 4, 4, this);
				g.drawImage(ColorManager.graph_lower_right,  this.getWidth() - 3 - SizeManager.graphImageSize, this.getHeight() - 3 - SizeManager.graphImageSize, this.getWidth() - 3, this.getHeight() - 3, 0, 0, 4, 4, this);
				g.drawImage(ColorManager.graph_side_left,    2, 2 + SizeManager.graphImageSize, 2 + SizeManager.graphImageSize, this.getHeight() - 3 - SizeManager.graphImageSize, 0, 0, 4, 56, this);
				g.drawImage(ColorManager.graph_upper_middle, 2 + SizeManager.graphImageSize, 2, this.getWidth() - 3 - SizeManager.graphImageSize, 2 + SizeManager.graphImageSize, 0, 0, 46, 4, this);
				g.drawImage(ColorManager.graph_lower_middle, 2 + SizeManager.graphImageSize, this.getHeight() - 3 - SizeManager.graphImageSize, this.getWidth() - 3 - SizeManager.graphImageSize, this.getHeight() - 3, 0, 0, 46, 4, this);
				g.drawImage(ColorManager.graph_side_right,   this.getWidth() - 3 - SizeManager.graphImageSize, 2 + SizeManager.graphImageSize, this.getWidth() - 3, this.getHeight() - 3 - SizeManager.graphImageSize, 0, 0, 4, 56, this);
			}catch(NullPointerException e){
				//catch but do not solve, this is caused by a race
				//condition. However adding synchronisation would impact
				//performance more then it is worth
			}
		}
	}

	/**
	 * Adds a new point to the end of this graph
	 * @param value The new point to add
	 */
	public void addPoint(int value){
		if(Main.config.showGraph){
			if(value > maxval){
				maxval = value;
			}
			values.addFirst(value);
			if(values.size() > Main.config.backlog){
				values.removeLast();
			}
		}
	}

	@Override
	public int getLayoutX(){
		return Main.config.graph_x;
	}

	@Override
	public int getLayoutY(){
		return Main.config.graph_y;
	}

	@Override
	public int getLayoutWidth(){
		return Main.config.graph_w;
	}

	@Override
	public int getLayoutHeight(){
		return Main.config.graph_h;
	}
}
