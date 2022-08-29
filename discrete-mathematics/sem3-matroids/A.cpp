#include <algorithm>
#include <iostream>
#include <fstream>
#include <queue>
#include <set>

std::vector<int> parent;

int find(int v)
{
    if (v == parent[v])
    {
        return v;
    }
    return parent[v] = find(parent[v]);
}

void unite(int a, int b)
{
    a = find(a);
    b = find(b);
    if (a != b)
    {
        parent[b] = a;
    }
}

struct CompareTask
{
    bool operator()(std::pair<int, int> v1, std::pair<int, int> v2)
    {
        return v1.second < v2.second;
    }
};

int main()
{
    std::ifstream in("schedule.in");
    std::ofstream out("schedule.out");

    int n;
    in >> n;

    parent.resize(n);
    for (int i = 0; i < n; ++i)
    {
        parent[i] = i;
    }

    std::priority_queue<std::pair<int, int>,
                        std::vector<std::pair<int, int>>,
                        CompareTask>
        tasks;

    for (int i = 0; i < n; ++i)
    {
        int d, w;
        in >> d >> w;
        tasks.push({d, w});
    }

    long long ans = 0;
    std::vector<bool> busy(n, false);
    while (!tasks.empty())
    {
        std::pair<int, int> task = tasks.top();
        tasks.pop();

        if (task.first == 0)
        {
            ans += task.second;
            continue;
        }

        int bound = find(std::min(task.first - 1, n - 1));
        if (!busy[bound])
        {
            busy[bound] = true;
            if (bound > 0)
            {
                unite(bound - 1, bound);
            }
        }
        else
        {
            ans += task.second;
        }
    }

    out << ans;
}