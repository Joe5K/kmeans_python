package paralel_kmeans;

import java.util.ArrayList;

class Point
{
    private double[] coordinates;
    Point(double[] coordinates)
    {
        this.coordinates = coordinates;
    }

    double[] getCoordinates(){
        return this.coordinates;
    }

    public void setCoordinates(double[] coordinates){
        this.coordinates = coordinates;
    }

    Point(ArrayList<Double> coordinates)
    {
        this.coordinates = new double[coordinates.size()];
        for (int i = 0; i < this.coordinates.length; i++){
            this.setOnIndex(i, coordinates.get(i));
        }
    }
    double getOnIndex(int index)
    {
        return this.coordinates[index];
    }
    void setOnIndex(int index, double data)
    {
        this.coordinates[index] = data;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof Point))
            return false;

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

    int getLength()
    {
        return this.getCoordinates().length;
    }

    double getDistance(Point vector) throws Exception {
        return Math.sqrt(this.getDistanceSquared(vector));
    }

    double getDistanceSquared(Point vector) throws Exception {
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