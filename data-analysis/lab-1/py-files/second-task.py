import numpy as np
import scipy.interpolate as interpolate
from matplotlib import pyplot as plt


def timer(number):
    def decorator(func):
        import time

        def wrapper(*args, **kwargs):
            result = 0
            for i in range(0, number):
                start = time.time()
                func(*args, **kwargs)
                end = time.time()
                result += end - start
            print("Method: ", func.__name__, ", Time:", result / number)
            return func(*args, **kwargs)

        return wrapper

    return decorator


@timer(100)
def other_lagrange_interpolation(x_m, y_m, xnew_m):
    return interpolate.lagrange(x_m, y_m)(xnew_m)


@timer(1000)
def my_lagrange_interpolation(x_m, y_m, xnew_m):
    ynew_m = np.zeros_like(xnew_m)
    for i in range(len(x_m)):
        p = y_m[i]
        for j in range(len(x_m)):
            if i != j:
                p *= (xnew_m - x_m[j]) / (x_m[i] - x_m[j])
        ynew_m += p
    return ynew_m


def draw(xnew, ynew, x, y):
    plt.scatter(x, y, c='r', marker='o')
    plt.plot(xnew, ynew)
    plt.show()


def main():
    n = np.random.randint(5, 20)
    x = np.random.uniform(0, 10, n)
    y = np.random.uniform(0, 10, n)

    x, y = zip(*sorted(zip(x, y)))

    xnew = np.linspace(x[0], x[-1], 100)
    other_ynew = other_lagrange_interpolation(x, y, xnew)
    my_ynew = my_lagrange_interpolation(x, y, xnew)

    draw(xnew, other_ynew, x, y)
    draw(xnew, my_ynew, x, y)


if __name__ == '__main__':
    main()
