from contextlib import suppress
from copy import deepcopy
from math import sqrt, inf

from numpy.random import choice
from numpy import array_split

class Point:
    def __init__(self, coordinates: list):
        self.coordinates = coordinates

    def __iter__(self):
        return iter(self.coordinates)

    def __repr__(self):
        return f"({', '.join([str(i) for i in self.coordinates])})"

    def __hash__(self):
        return hash(repr(self))

    def __eq__(self, other):
        for i, j in zip(self.coordinates, other.coordinates):
            if i != j:
                return False
        return True

    def distance(self, point):
        return sqrt(sum([(i - j) ** 2 for i, j in zip(self.coordinates, point.coordinates)]))


class Centroid:
    def __init__(self, centroid_coordinate: Point):
        self.centroid_coordinate = centroid_coordinate
        self.points = []

    def __repr__(self):
        return str(self.centroid_coordinate)

    def __eq__(self, other):
        return self.centroid_coordinate == other.centroid_coordinate and set(self.points) == set(other.points)

    def count_new_coordinates(self):
        if self.points:
            sum_vector = [0 for _ in self.points[0]]
            for i in self.points:
                for index, j in enumerate(i):
                    sum_vector[index] += j
            self.centroid_coordinate = Point([i / len(self.points) for i in sum_vector])


class KMeans:
    def __init__(self, data_filename: str, k: int = 10):
        self._load_data(data_filename)
        self._generate_initial_centroids(k)

    def _load_data(self, filename):
        self.points = []
        with open(filename) as file:
            dimension = None
            for line in file:
                data = line.replace(",", ".").replace("\n", "").split(";")

                floats = []
                for i in data:
                    with suppress(ValueError):
                        floats.append(float(i))

                if not dimension:
                    dimension = len(floats)
                elif dimension != len(floats):
                    raise ValueError

                if floats:
                    self.points.append(Point(floats))

    def _generate_initial_centroids(self, k):
        self.centroids = [Centroid(i) for i in choice(self.points, k, replace=False)]

    def work(self):
        counter = 0
        successful = False

        while not successful:
            _previous_centroids = deepcopy(self.centroids)

            for i in self.centroids:
                i.points = []

            for i in self.points:
                new_centroid = None
                distance_to_new_centroid = inf
                for j in self.centroids:
                    current_distance = i.distance(j.centroid_coordinate)
                    if current_distance < distance_to_new_centroid:
                        new_centroid = j
                        distance_to_new_centroid = current_distance
                new_centroid.points.append(i)

            for i in self.centroids:
                i.count_new_coordinates()

            successful = True
            for i, j in zip(self.centroids, _previous_centroids):
                if i != j:
                    successful = False
                    counter += 1
                    break

        print(counter)


k_means = KMeans("iris.csv", 3)
k_means.work()
pass
