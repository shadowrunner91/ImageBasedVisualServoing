package at.ac.uibk.cs.auis.ImageBasedVisualServoing.Common;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import android.os.Parcel;
import android.os.Parcelable;

public class CalibrationHelper implements Parcelable, Serializable {
	private static final long serialVersionUID = 4529667799595429110L;

	private ArrayList<ParcelablePoint> imagePlaneCoordinates = new ArrayList<ParcelablePoint>();
	private ArrayList<ParcelablePoint> worldCoordinates = new ArrayList<ParcelablePoint>();
	public final int maxNumPoints = 4;

	private Mat _cachedImagePlane2WorldCoordinates;

	public CalibrationHelper() {
		worldCoordinates.add(new ParcelablePoint(175.0, 125.0));
		worldCoordinates.add(new ParcelablePoint(175.0, 25.0));
		worldCoordinates.add(new ParcelablePoint(275.0, 25.0));
		worldCoordinates.add(new ParcelablePoint(275.0, 125.0));
	}

	public void addImagePlaneCoordinates(int pointNumber, Point point) {
		if (pointNumber <= 0 || pointNumber > maxNumPoints)
			throw new IllegalArgumentException("only" + maxNumPoints
					+ " points are allowed.");
		if (point == null)
			throw new IllegalArgumentException("point must not be null");

		imagePlaneCoordinates.add(pointNumber - 1, new ParcelablePoint(point));
	}

	public Point getWorldCoordinates(int pointNumber) {
		if (pointNumber <= 0 || pointNumber > maxNumPoints)
			throw new IllegalArgumentException("only" + maxNumPoints
					+ " points are allowed.");

		return worldCoordinates.get(pointNumber - 1);
	}

	public List<String> getSummary() {
		List<String> strings = new ArrayList<String>();
		strings.add("Image-Plane -> Ground-Plane");

		for (int i = 0; i < maxNumPoints; i++)
			strings.add(PointToString(imagePlaneCoordinates.get(i)) + " -> "
					+ PointToString(worldCoordinates.get(i)));

		return strings;
	}

	public String PointToString(Point point) {
		return "(" + point.x + ", " + point.y + ")";
	}
	
	public Mat getHomogenousMat() {
		if(_cachedImagePlane2WorldCoordinates!=null)
			return _cachedImagePlane2WorldCoordinates;
		
		calculateHomogenousMat();
		return _cachedImagePlane2WorldCoordinates;
	}

	private void calculateHomogenousMat() {
		Mat src = new Mat(4, 1, CvType.CV_32FC2);
		src.put(0, 0, new double[] {imagePlaneCoordinates.get(0).x, imagePlaneCoordinates.get(0).y});
		src.put(1, 0, new double[] {imagePlaneCoordinates.get(1).x, imagePlaneCoordinates.get(1).y});
		src.put(2, 0, new double[] {imagePlaneCoordinates.get(2).x, imagePlaneCoordinates.get(2).y});
		src.put(3, 0, new double[] {imagePlaneCoordinates.get(3).x, imagePlaneCoordinates.get(3).y});
		
		Mat dest = new Mat(4, 1, CvType.CV_32FC2);
		dest.put(0, 0, new double[] {worldCoordinates.get(0).x, worldCoordinates.get(0).y});
		dest.put(1, 0, new double[] {worldCoordinates.get(1).x, worldCoordinates.get(1).y});
		dest.put(2, 0, new double[] {worldCoordinates.get(2).x, worldCoordinates.get(2).y});
		dest.put(3, 0, new double[] {worldCoordinates.get(3).x, worldCoordinates.get(3).y});
		
		Mat worldCorrdinates2imagePlaneCoordinates = Imgproc.getPerspectiveTransform(src, dest);
		
		//_cachedImagePlane2WorldCoordinates = worldCorrdinates2imagePlaneCoordinates.inv();
		_cachedImagePlane2WorldCoordinates = worldCorrdinates2imagePlaneCoordinates;
	}

	/********************** Parceling **********************/
	// see
	// http://stackoverflow.com/questions/7042272/how-to-properly-implement-parcelable-with-an-arraylistparcelable
	// and
	// http://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents
	@Override
	public int describeContents() {
		return 0;
	}

	// write your object's data to the passed-in Parcel
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeTypedList(imagePlaneCoordinates);
		out.writeTypedList(worldCoordinates);
	}

	// this is used to regenerate your object. All Parcelables must have a
	// CREATOR that implements these two methods
	public static final Parcelable.Creator<CalibrationHelper> CREATOR = new Parcelable.Creator<CalibrationHelper>() {
		public CalibrationHelper createFromParcel(Parcel in) {
			return new CalibrationHelper(in);
		}

		public CalibrationHelper[] newArray(int size) {
			return new CalibrationHelper[size];
		}
	};

	// example constructor that takes a Parcel and gives you an object populated
	// with it's values
	private CalibrationHelper(Parcel in) {
		in.readTypedList(imagePlaneCoordinates, ParcelablePoint.CREATOR);
		in.readTypedList(worldCoordinates, ParcelablePoint.CREATOR);
	}
	/********************** end-Parceling **********************/

	/********************** Serializing **********************/
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(imagePlaneCoordinates);
		out.writeObject(worldCoordinates);
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		imagePlaneCoordinates = (ArrayList<ParcelablePoint>) in.readObject();
		worldCoordinates = (ArrayList<ParcelablePoint>) in.readObject();
		_cachedImagePlane2WorldCoordinates = null;
	}
	/********************** end-Serializing **********************/

}

