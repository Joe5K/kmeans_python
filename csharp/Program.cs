using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace paralel_kmeans
{
    class Point
    {
        public double[] Coordinates;
        public Point(double[] coordinates)
        {
            this.Coordinates = coordinates;
        }
        public double this[int index]
        {
            get => this.Coordinates[index];
            set => this.Coordinates[index] = value;
        }

        public override bool Equals(object obj)
        {
            Point otherPoint = (Point)obj;
            for (int i = 0; i < this.Length; i++)
            {
                if (this[i] != otherPoint[i])
                {
                    return false;
                }
            }
            return true;
        }

        public int Length { get => this.Coordinates.Length; }

        public override int GetHashCode()
        {
            return base.GetHashCode();
        }

        internal double Distance(Point vector)
        {
            return Math.Sqrt(DistanceSquared(vector));
        }

        internal double DistanceSquared(Point vector)
        {
            if (this.Length != vector.Length)
                throw new Exception();
            double sum = 0.0;
            for (int i = 0; i < this.Length; i++)
            {
                sum += Math.Pow(this[i] - vector[i], 2);
            }
            return sum;
        }

        public override string ToString()
        {
            return "(" + string.Join(", ", this.Coordinates.Select(x => Math.Round(x, 4))) + ")";
        }

    }

    class Cluster 
    {
        public Point Centroid;
        public List<Point> Points;
        public Cluster(Point Centroid)
        {
            this.Centroid = Centroid;
            this.Points = new List<Point>();
        }

        public void CountNewCentroids()
        {
            if (this.Points.Count == 0)
                return;

            var sumVector = new decimal[this.Points[0].Length];
            foreach (var point in this.Points)
            {
                for (int i = 0; i < point.Length; i++)
                {
                    sumVector[i] += (decimal)point[i];
                }
            }
            for (int i = 0; i < sumVector.Length; i++)
            {
                this.Centroid[i] = (double)(sumVector[i] / this.Points.Count);
            }
        }

        public bool CentroidsEquals(Point otherCentroid)
        {
            return this.Centroid.Equals(otherCentroid);
        }
    }

    class KMeans
    {
        List<Cluster> Clusters = new List<Cluster>();
        List<Point> Points = new List<Point>();
        int ThreadsCount;
        public KMeans(string dataFilename, int k, int threadsCount)
        {
            this.LoadData(dataFilename);
            this.GenerateClustersWithInitialCentroids(k);
            this.ThreadsCount = threadsCount;
        }

        private void LoadData(string filename) {
            using (var reader = new StreamReader(filename))
            {
                int dimension = 0;
                while (!reader.EndOfStream)
                {
                    string[] data;
                    string line = reader.ReadLine();
                    if (line.Contains(",") && line.Contains(";"))
                        data = line.Replace(",", ".").Split(";");
                    else
                        data = line.Split(",");

                    var loadedFloats = new List<double>();
                    foreach (var i in data)
                    {
                        double f = 0;
                        if (double.TryParse(i, NumberStyles.Any, CultureInfo.InvariantCulture, out f))
                        {
                            loadedFloats.Add(f);
                        }
                    }

                    if (dimension == 0)
                    {
                        dimension = loadedFloats.Count;
                    }
                    else if (dimension != loadedFloats.Count)
                    {
                        throw new ApplicationException();
                    }

                    if (loadedFloats.Count > 0)
                    {
                        this.Points.Add(new Point(loadedFloats.ToArray()));
                    }
                    
                }
            }
            return;
        }

        private void GenerateClustersWithInitialCentroids(int k)
        {

            var random = new Random();
            foreach (var point in this.Points.OrderBy(x => random.Next()).Take(k))
            {
                double[] centroidCoordinates = new double[point.Coordinates.Length];
                Array.Copy(point.Coordinates, centroidCoordinates, point.Coordinates.Length);
                this.Clusters.Add(new Cluster(new Point(centroidCoordinates)));
            }
        }

        private static readonly Object obj = new Object();

        public void Work()
        {
            var splitDataForThreads = new List<Point>[this.ThreadsCount];
            for (int i = 0; i < this.ThreadsCount; i++)
            {
                splitDataForThreads[i] = new List<Point>();
            }

            for (int i = 0; i < this.Points.Count; i++)
            {
                int threadId = i % this.ThreadsCount;
                splitDataForThreads[threadId].Add(this.Points[i]);
            }

            var counter = 0;
            var successful = false;

            Console.WriteLine($"Pociatocny vyber centroidov: {string.Join(", ", this.Clusters.Select((x) => x.Centroid))}");

            Stopwatch stopwatch = new Stopwatch();
            stopwatch.Start();

            for (; !successful; counter++)
            {
                var previousCentroids = Helpers.CopyCentroids(this.Clusters);

                foreach (var cluster in this.Clusters)
                {
                    cluster.Points.Clear();
                }

                Thread[] threads = new Thread[this.ThreadsCount];
                for (int i = 0; i < this.ThreadsCount; i++)
                {
                    var data = splitDataForThreads[i];
                    threads[i] = new Thread(() =>
                    {
                        foreach (var point in data)
                        {
                            Cluster newCluster = null;
                            var shortestDistanceToCentroid = double.MaxValue;
                            foreach (var cluster in this.Clusters)
                            {
                                var currentDistance = point.Distance(cluster.Centroid);
                                if (currentDistance < shortestDistanceToCentroid)
                                {
                                    newCluster = cluster;
                                    shortestDistanceToCentroid = currentDistance;
                                }
                            }
                            lock (obj)
                            {
                                newCluster.Points.Add(point);
                            }
                        }
                    });
                    threads[i].Start();
                }

                for (int i = 0; i < this.ThreadsCount; i++)
                {
                    threads[i].Join();
                }

                foreach (var cluster in this.Clusters)
                {
                    cluster.CountNewCentroids();
                }

                Console.WriteLine($"Ukoncena {counter + 1}. iteracia, SSE = {this.CountSse()}. Pocty pointov pre jednotlive centroidy: {string.Join(", ", this.Clusters.Select(x => x.Points.Count))}. Suradnice centroidov: {string.Join(", ", this.Clusters.Select(x => x.Centroid.ToString()))}");

                successful = true;
                for (int i = 0; i < this.Clusters.Count; i++)
                {
                    if (!this.Clusters[i].CentroidsEquals(previousCentroids[i]))
                    {
                        successful = false;
                        break;
                    }
                }
                
            }

            stopwatch.Stop();

            Console.WriteLine($"KMeans ukoncene po {counter} iteraciach, trvalo to {Math.Round(stopwatch.Elapsed.TotalSeconds, 4)} sekund, pri {this.ThreadsCount} threadoch trvala jedna iteracia priemerne {Math.Round(stopwatch.Elapsed.TotalSeconds / counter, 4)} sekund.");
        }

        private double CountSse()
        {
            double sse = 0.0;
            foreach (var cluster in this.Clusters)
            {
                foreach (var point in cluster.Points)
                {
                    sse += point.DistanceSquared(cluster.Centroid);
                }
            }
            return Math.Round(sse, 4);
        }

    }
    class Program
    {
        static void Main(string[] args)
        {
            var kmeans = new KMeans(@"data/iris.csv", 3, 4);
            kmeans.Work();
        }
    }
}
