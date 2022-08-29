#include <iostream>
#include <vector>
#include <cmath>
#include <map>
#include <string>
#include <algorithm>

std::vector<bool> used;
std::vector<int> matching;
std::map<int, std::vector<int>> adjacent;

bool kuhn(int vertex)
{
    if (used[vertex])
    {
        return false;
    }
    used[vertex] = true;
    for (int next : adjacent[vertex])
    {
        if (matching[next] == -1 || kuhn(matching[next]))
        {
            matching[next] = vertex;
            return true;
        }
    }
    return false;
}

bool dfs(int vertex)
{
    if (used[vertex])
    {
        return false;
    }
    used[vertex] = true;
    if (matching[vertex] != -1)
    {
        dfs(matching[vertex]);
    }
    return true;
}

int v;

class Point
{
private:
    int x, y;
    std::string time;

public:
    Point(int x, int y, std::string time)
        : x(x), y(y), time(time) {}

    double distTo(Point p)
    {
        return sqrt(pow(p.x - x, 2) + pow(p.y - y, 2));
    }

    int getTime()
    {
        return std::stoi(time.substr(0, 2)) * 60 + std::stoi(time.substr(3, 5));
    }

    friend bool operator<(Point &p1, Point &p2)
    {
        return p1.getTime() < p2.getTime();
    }

    friend std::ostream &operator<<(std::ostream &out, Point &p1)
    {
        out << "{ " << p1.x << ", " << p1.y << " } " << p1.time << std::endl;
        return out;
    }
};

int main()
{
    std::ios::sync_with_stdio(false);
    std::cin.tie(nullptr);

    int n;
    std::cin >> n >> v;

    std::vector<Point> points;
    for (int i = 0; i < n; i++)
    {
        std::string time;
        int x, y;
        std::cin >> time >> x >> y;

        points.push_back(Point(x, y, time));
    }

    std::sort(points.begin(), points.end());

    for (int i = 0; i < n; ++i)
    {
        for (int j = i + 1; j < n; ++j)
        {
            double dist = points[i].distTo(points[j]);
            double spentTime = (dist / v) * 60;
            if (points[i].getTime() + spentTime <= points[j].getTime())
            {
                adjacent[i].push_back(j);
            }
        }
    }

    matching.assign(n, -1);
    for (int i = 0; i < n; i++)
    {
        used.assign(n, false);
        kuhn(i);
    }

    int ans = 0;
    used.assign(n, false);
    for (int i = n - 1; i >= 0; i--)
    {
        ans += dfs(i);
    }
    std::cout << ans << std::endl;
}