package at.ac.uibk.cs.auis.ImageBasedVisualServoing.Common;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class DrawHelper {
	public static Mat drawPoint(Mat picture, Point point, Scalar color) {
		if(point==null)
			return picture;
		
		int centerMaxX = (int) (point.x + 6 / 2);
		int centerMinX = (int) (point.x - 6 / 2);
		int centerMaxY = (int) (point.y + 6 / 2);
		int centerMinY = (int) (point.y - 6 / 2);

		if (centerMaxX > picture.width() - 1)
			centerMaxX = picture.width() - 1;

		if (centerMaxY > picture.height() - 1)
			centerMaxY = picture.height() - 1;

		if (centerMinX < 0)
			centerMinX = 0;

		if (centerMinY < 0)
			centerMinY = 0;

		Mat subMatColorMass = picture.submat(centerMinY, centerMaxY,
				centerMinX, centerMaxX);
		subMatColorMass.setTo(color);
		
		Core.circle(picture, point, 10, color);

		return picture;
	}

	public static Mat drawRectangle(Mat picture, Rect rec, Scalar color) {
		Core.rectangle(picture, rec.tl(), rec.br(), color);
		return picture;
	}
}
