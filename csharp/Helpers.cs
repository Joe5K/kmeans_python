using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace paralel_kmeans
{
    public static class Helpers
    {

        internal static List<Point> CopyCentroids(List<Cluster> centroids)
        {
            List<Point> oldCentroids = new List<Point>();
            foreach (var centroid in centroids)
            {
                double[] centroidCoordinates = new double[centroid.Centroid.Length];
                Array.Copy(centroid.Centroid.Coordinates, centroidCoordinates, centroid.Centroid.Length);
                oldCentroids.Add(new Point(centroidCoordinates));
            }
            return oldCentroids;
        }
    }
}
