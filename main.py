from contextlib import suppress
from datetime import datetime
from math import sqrt, inf
from threading import Thread

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

    def __getitem__(self, item):
        return self.coordinates[item]

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

    def count_new_coordinates(self):
        if self.points:
            sum_vector = [0 for _ in self.points[0]]
            for i in self.points:
                for index, j in enumerate(i):
                    sum_vector[index] += j
            self.centroid_coordinate = Point([i / len(self.points) for i in sum_vector])


class KMeans:
    def __init__(self, data_filename: str, k: int, threads_count: int):
        self._load_data(data_filename)
        self._generate_initial_centroids(k)
        self.threads_count = threads_count

    def _load_data(self, filename):
        self.points = []
        with open(filename) as file:
            dimension = None
            for line in file:
                if filename == "iris.csv":
                    data = line.replace(",", ".").replace("\n", "").split(";")
                else:
                    data = line.replace("\n", "").split(",")

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

    def _thread_function(self, points):
        for i in points:
            new_centroid = None
            distance_to_new_centroid = inf
            for j in self.centroids:
                current_distance = i.distance(j.centroid_coordinate)
                if current_distance < distance_to_new_centroid:
                    new_centroid = j
                    distance_to_new_centroid = current_distance
            new_centroid.points.append(i)  # list.append is thread safe operation

    def work(self):
        split_data_for_threads = array_split(self.points, self.threads_count)

        counter = 0
        successful = False

        start = datetime.now()
        while not successful:
            _previous_centroids = [Centroid(i.centroid_coordinate) for i in self.centroids]

            for i in self.centroids:
                i.points.clear()

            threads = [Thread(target=self._thread_function(i)) for i in split_data_for_threads]
            for i in threads:
                i.start()

            for i in threads:
                i.join()

            for i in self.centroids:
                i.count_new_coordinates()

            successful = True
            for i, j in zip(self.centroids, _previous_centroids):
                if i.centroid_coordinate != j.centroid_coordinate:
                    successful = False
                    break
            counter += 1
        time = round((datetime.now()-start).total_seconds(), 3)
        print(f"""KMeans ukoncene po {counter} iteraciach, trvalo to {time} sekund, pri {self.threads_count} threadoch \
trvala jedna iteracia priemerne {round(time/counter, 3)} sekund.""")

    def plot(self):
        import matplotlib.pyplot as plt

        fig = plt.figure(figsize=(12, 12))
        ax = fig.add_subplot(projection='3d')

        for i in self.centroids:
            ax.scatter([j[0] for j in i.points], [j[1] for j in i.points], [j[2] for j in i.points])
        plt.show()


k_means = KMeans(data_filename="diamonds_numeric.csv", k=3, threads_count=4)
k_means.work()
k_means.plot()
