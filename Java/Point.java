package paralel_kmeans;

import java.util.ArrayList;

public class Point
{
    private double[] coordinates;
    public Point(double[] coordinates)
    {
        this.coordinates = coordinates;
    }

    public double[] getCoordinates(){
        return this.coordinates;
    }

    public void setCoordinates(double[] coordinates){
        this.coordinates = coordinates;
    }

    public Point(ArrayList<Double> coordinates)
    {
        this.coordinates = new double[coordinates.size()];
        for (int i = 0; i < this.coordinates.length; i++){
            this.setOnIndex(i, coordinates.get(i));
        }
    }
    public double getOnIndex(int index)
    {
        return this.coordinates[index];
    }
    public void setOnIndex(int index, double data)
    {
        this.coordinates[index] = data;
    }

    public boolean equals(Object obj)
    {
        Point other_point = (Point)obj;
        for (int i = 0; i < this.getLength(); i++)
        {
            if (this.getOnIndex(i) != other_point.getOnIndex(i))
            {
                return false;
            }
        }
        return true;
    }

    public int getLength()
    {
        return this.coordinates.length;
    }

    public double getDistance(Point vector) throws Exception {
        return Math.sqrt(this.getDistanceSquared(vector));
    }

    public double getDistanceSquared(Point vector) throws Exception {
        if (this.getLength() != vector.getLength()){
            throw new Exception();
        }
        double sum = 0.0;
        for( int i = 0; i< this.getLength(); i++){
            sum += Math.pow(this.getOnIndex(i) - vector.getOnIndex(i), 2);
        }
        return sum;
    }

    @Override
    public String toString() {
        var array = new String[this.getLength()];
        for (int i = 0; i < this.getLength(); i++){
            array[i] = Double.toString(Helpers.round(this.getOnIndex(i), 4));
        }
        return "(" + String.join(", ", array) + ")";
    }
}