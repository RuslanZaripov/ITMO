#include <iostream>
#include <iomanip>
#include <vector>
#include <queue>
#include <cmath>
#include <deque>
#include <set>
#include <map>

const double INF = 1e9;

double dist(std::pair<int, int> p1, std::pair<int, int> p2)
{
    int dx = p1.first - p2.first;
    int dy = p1.second - p2.second;
    return std::sqrt(dx * dx + dy * dy);
}

int main()
{
    int n;
    std::cin >> n;

    std::vector<std::pair<int, int>> pointList;
    std::vector<bool> visited(n);
    std::vector<double> distToPoint(n);

    for (int i = 0; i < n; ++i)
    {
        int x, y;
        std::cin >> x >> y;
        std::pair<int, int> point = { x, y };

        pointList.push_back(point);
        visited[i] = false;
        distToPoint[i] = INF;
    }

    double mstMinWeight = 0.0;
    int edgeNumber = -1;
    distToPoint[0] = 0.0;

    while (edgeNumber != pointList.size() - 1)
    {
        int index = -1;
        double mstDiff = INF;

        for (int i = 0; i < n; i++)
        {
            if (!visited[i] && distToPoint[i] < mstDiff)
            {
                mstDiff = distToPoint[i];
                index = i;
            }
        }

        visited[index] = true;
        mstMinWeight += mstDiff;
        edgeNumber++;

        for (int i = 0; i < n; i++)
        {
            double distToNextPoint = dist(pointList[index], pointList[i]);
            if (!visited[i] && distToNextPoint < distToPoint[i])
            {
                distToPoint[i] = distToNextPoint;
            }
        }
    }

    std::cout << std::setprecision(11) << mstMinWeight << std::endl;
}