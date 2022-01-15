package paralel_kmeans;

public class Main {

    public static void main(String[] args) throws Exception {
        var kmeans = new KMeans("../data/iris.csv", 3, 4);
        kmeans.work();
    }
}
