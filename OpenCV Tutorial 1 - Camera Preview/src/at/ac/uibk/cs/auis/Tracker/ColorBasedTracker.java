package at.ac.uibk.cs.auis.Tracker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ColorBasedTracker {

	private static final double MINIMAL_VALUE_OF_CONTOUR_AREA = 0.1;
	
	private Mat blackWhiteMask;
	private Mat dilatedMask;

	private Scalar colorForTrackingHSV;
	private Scalar colorRadius = new Scalar(25, 25, 25, 0);
	private Scalar lowerBound;
	private Scalar upperBound;

	private List<MatOfPoint> contours;
	private List<Rect> boundingRects = new ArrayList<Rect>();
	private List<Point> trackPath = new ArrayList<Point>();
	
	/**
	 * initializes the bounds used for range checking in HSV-color-space
	 */
	private void initializeBounds() {
		lowerBound = new Scalar(0);
		upperBound = new Scalar(0);
		lowerBound.val[0] = colorForTrackingHSV.val[0] - colorRadius.val[0];
		upperBound.val[0] = colorForTrackingHSV.val[0] + colorRadius.val[0];
		lowerBound.val[1] = colorForTrackingHSV.val[1] - colorRadius.val[1];
		upperBound.val[1] = colorForTrackingHSV.val[1] + colorRadius.val[1];
		lowerBound.val[2] = colorForTrackingHSV.val[2] - colorRadius.val[2];
		upperBound.val[2] = colorForTrackingHSV.val[2] + colorRadius.val[2];
		lowerBound.val[3] = 0;
		upperBound.val[3] = 255;
	}

	
	/**
	 * calculates the center of mass using <code>colorForTrackingHSV</code> and <code>colorRadius</code> as radius (in HSV-color space)
	 * @param hsv
	 *  the frame of which the center of mass should be calculated off
	 * @return
	 *  the center of mass as a point in pixel coordinates (i.e. integer)
	 */
	public Point calcCenterOfMass(Mat hsv) {
		boundingRects.clear();
		
		blackWhiteMask = new Mat();
		Core.inRange(hsv, lowerBound, upperBound, blackWhiteMask);

		dilatedMask = new Mat();
		Imgproc.dilate(blackWhiteMask, dilatedMask, new Mat());

		contours = new ArrayList<MatOfPoint>();
		Mat mHierarchy = new Mat();
		Mat tempDilatedMask = dilatedMask.clone();
		Imgproc.findContours(tempDilatedMask, contours, mHierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		MatOfPoint largestContour = findLargestContours(contours, 1)[0];
		if(largestContour==null)
			throw new IllegalArgumentException();
		
		boundingRects.add(Imgproc.boundingRect(largestContour));

		int centerOfMassX = boundingRects.get(0).x + boundingRects.get(0).width / 2;
		int centerOfMassY = boundingRects.get(0).y + boundingRects.get(0).height / 2;

		Point centerOfMass = new Point(centerOfMassX, centerOfMassY);
		trackPath.add(centerOfMass);
		return centerOfMass;
	}
	
	/**
	 * calculates the lowest points (i.e. the points that SHOULD touch the ground) of
	 * @code numOfContours -Points and returns them as an array (which HAS have size numOfContours iff that many contours exist)
	 */
	public Point[] getLowestBoundOfContours(Mat hsv, int numOfContours) {
		boundingRects.clear();
		
		blackWhiteMask = new Mat();
		Core.inRange(hsv, lowerBound, upperBound, blackWhiteMask);

		dilatedMask = new Mat();
		Imgproc.dilate(blackWhiteMask, dilatedMask, new Mat());

		contours = new ArrayList<MatOfPoint>();
		Mat mHierarchy = new Mat();
		Mat tempDilatedMask = dilatedMask.clone();
		Imgproc.findContours(tempDilatedMask, contours, mHierarchy,
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		MatOfPoint[] largestContours = findLargestContours(contours, numOfContours);
		Point[] lowestPoints = new Point[largestContours.length];
		
		int i=0;
		for(MatOfPoint matOfPoints : largestContours) {
			if(matOfPoints!=null) {
				lowestPoints[i++] = getLowestPointOfContour(matOfPoints);
				boundingRects.add(Imgproc.boundingRect(matOfPoints));
			}
		}
		
		return lowestPoints;
	}

	/*
	 * (non-javadoc)
	 * only looks at the y-coordinate of the point;
	 * the point with the highest one is the nearest to the ground
	 */
	private Point getLowestPointOfContour(MatOfPoint matOfPoints) {
		Point nearestPointToGround = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		for(Point point : matOfPoints.toArray()) {
			if(point.y>nearestPointToGround.y) {
				nearestPointToGround = point;
			}
		}
		return nearestPointToGround;
	}


	private MatOfPoint[] findLargestContours(List<MatOfPoint> contours, int numOfCounters) {
		Map<Double, MatOfPoint> lookup = new TreeMap<Double, MatOfPoint>(new Comparator<Double>() {

			@Override
			public int compare(Double lhs, Double rhs) {
				return (int)(rhs - lhs);
					
			}
			
		});
		
		// Find max contour area
		MatOfPoint maxAreaMatrix = new MatOfPoint();

		double maxArea = 0;

		Iterator<MatOfPoint> each = contours.iterator();
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			double area = Imgproc.contourArea(wrapper);
			
			lookup.put(area, wrapper);
			
			/*if (area > maxArea) {
				maxArea = area;
				maxAreaMatrix = wrapper;
			}*/
		}

		MatOfPoint[] returnArray = new MatOfPoint[numOfCounters];
		int i=0;
		for(Map.Entry<Double, MatOfPoint> entry : lookup.entrySet()) {
			if(i==numOfCounters)
				break;
			
			returnArray[i++] = entry.getValue();
		}
		
		return returnArray;
	}

	public Scalar getColorForTrackingHSV() {
		return colorForTrackingHSV;
	}

	public void setColorForTrackingHSV(Scalar colorForTrackingHSV) {
		this.colorForTrackingHSV = colorForTrackingHSV;
		initializeBounds();
	}

	public Scalar getColorRadius() {
		return colorRadius;
	}

	public void setColorRadius(Scalar colorRadius) {
		this.colorRadius = colorRadius;
	}

	public Mat getBlackWhiteMask() {
		return blackWhiteMask;
	}

	public Mat getDilatedMask() {
		return dilatedMask;
	}

	/**
	 * @return the surrounding of the rectangle
	 */
	public List<Rect> getBoundingRects() {
		return boundingRects;
	}


	public List<MatOfPoint> getContour() {
		return contours;
	}


	public List<Point> getTrackPath() {
		return trackPath;
	}
	
}
