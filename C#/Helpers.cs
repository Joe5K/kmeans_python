using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace paralel_kmeans
{
    internal static class Helpers
    {
        internal static List<Point> CopyCentroids(List<Cluster> clusters)
        {
            List<Point> oldCentroids = new List<Point>();
            foreach (var cluster in clusters)
            {
                double[] centroidCoordinates = new double[cluster.Centroid.Length];
                Array.Copy(cluster.Centroid.Coordinates, centroidCoordinates, cluster.Centroid.Length);
                oldCentroids.Add(new Point(centroidCoordinates));
            }
            return oldCentroids;
        }
    }
}
