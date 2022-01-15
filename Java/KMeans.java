package paralel_kmeans;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Random;

public class KMeans
{
    private ArrayList<Cluster> clusters = new ArrayList<Cluster>();
    private ArrayList<Point> points = new ArrayList<Point>();
    private int threadsCount;

    public KMeans(String dataFilename, int k, int threadsCount)
    {
        this.loadData(dataFilename);
        this.generateInitialCentroids(k);
        this.threadsCount = threadsCount;
    }

    private void loadData(String filename) {
        try {
            int dimension = 0;
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                String[] data;
                if (line.contains(",") && line.contains(";"))
                    data = line.replace(",",".").split(";");
                else
                    data = line.split(",");
                var floats = new ArrayList<Double>();
                for (var i: data)
                {
                    try{floats.add(Double.parseDouble(i));}
                    catch(NumberFormatException ignored){}
                }

                if (dimension == 0)
                {
                    dimension = floats.size();
                }
                else if (dimension != floats.size())
                {
                    throw new RuntimeException();
                }

                if (floats.size() > 0)
                {
                    this.points.add(new Point(floats));
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void generateInitialCentroids(int k)
    {
        var random = new Random();

        var centroidIndexes = new ArrayList<Integer>();
        while (centroidIndexes.size() < k){
            var newIndex = random.nextInt(this.points.size());
            if (!centroidIndexes.contains(newIndex)){
                centroidIndexes.add(newIndex);
            }
        }

        for(var i: centroidIndexes){
            var point = this.points.get(i);
            var centroidCoordinates = Arrays.copyOf(point.getCoordinates(), point.getLength());
            this.clusters.add(new Cluster(new Point(centroidCoordinates)));
        }
    }

    private final Object lock = new Object();

    public void work() throws Exception {
        var splitDataForThreads = new ArrayList[this.threadsCount];
        for (int i = 0; i < this.threadsCount; i++)
        {
            splitDataForThreads[i] = new ArrayList<Point>();
        }

        for (int i = 0; i < this.points.size(); i++)
        {
            int threadId = i % this.threadsCount;
            splitDataForThreads[threadId].add(this.points.get(i));
        }

        {
            var centroidsStrings = new String[this.clusters.size()];
            for (int i = 0; i < this.clusters.size(); i++){
                centroidsStrings[i] = this.clusters.get(i).getCentroid().toString();
            }
            System.out.println("Pociatocny vyber centroidov: " + String.join(", ", centroidsStrings));

        }

        var counter = 0;
        var successful = false;

        long start = System.currentTimeMillis();

        for (; !successful; counter++)
        {
            var previousCentroids = Helpers.copyCentroids(this.clusters);

            for (var cluster: this.clusters)
            {
                cluster.getPoints().clear();
            }

            Thread[] threads = new Thread[this.threadsCount];
            for (int i = 0; i < this.threadsCount; i++)
            {
                ArrayList<Point> data = splitDataForThreads[i];
                threads[i] = new Thread(() -> {
                    for (var point: data)
                    {
                        Cluster newCluster = null;
                        var shortestDistanceToCentroid = Double.MAX_VALUE;
                        for (var cluster: clusters)
                        {
                            double currentDistance = 0;
                            try {
                                currentDistance = point.getDistance(cluster.getCentroid());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (currentDistance < shortestDistanceToCentroid)
                            {
                                newCluster = cluster;
                                shortestDistanceToCentroid = currentDistance;
                            }
                        }
                        synchronized (lock)
                        {
                            newCluster.getPoints().add(point);
                        }
                    }
                });
                threads[i].start();
            }

            for (int i = 0; i < this.threadsCount; i++)
            {
                threads[i].join();
            }

            for (var cluster: this.clusters)
            {
                cluster.countNewCoordinates();
            }

            {
                var centroidsStrings = new String[this.clusters.size()];
                var pointCounts = new String[this.clusters.size()];
                for (int i = 0; i < this.clusters.size(); i++){
                    centroidsStrings[i] = this.clusters.get(i).getCentroid().toString();
                    pointCounts[i] = Integer.toString(this.clusters.get(i).getPoints().size());
                }

                System.out.println("Ukoncena " + Integer.toString(counter+1) + ". iteracia, SSE = " + this.countSse() + ". Pocty pointov pre jednotlive centroidy: " + String.join(", ", pointCounts) + ". Suradnice centroidov: " + String.join(", ", centroidsStrings));

            }

            successful = true;
            for (int i = 0; i < this.clusters.size(); i++)
            {
                if (!this.clusters.get(i).centroidEquals(previousCentroids.get(i)))
                {
                    successful = false;
                    break;
                }
            }

        }

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("KMeans ukoncene po " + counter + " iteraciach, trvalo to "+Helpers.miliSecondsToSeconds(timeElapsed)+" sekund, pri "+threadsCount+" threadoch trvala jedna iteracia priemerne "+ Helpers.round(Helpers.miliSecondsToSeconds(timeElapsed) / counter, 4) +" sekund.");
        }

        private double countSse() throws Exception {
            double sse = 0.0;
            for (var cluster: this.clusters)
            {
                for (var point: cluster.getPoints())
                {
                    sse += point.getDistanceSquared(cluster.getCentroid());
                }
        }
        return Helpers.round(sse, 4);
    }

}
