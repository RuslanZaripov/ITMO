#include <algorithm>
#include <iostream>
#include <fstream>
#include <vector>

std::vector<std::vector<int>> adjList;
std::vector<int> l, r, order;
std::vector<bool> used;

struct Vertex
{
    int index;
    int weight;

    Vertex(int m_index, int m_weight)
        : index(m_index),
        weight(m_weight) {}
};

bool sortByWeight(const Vertex& a, const Vertex& b)
{
    return a.weight > b.weight;
}

bool dfs(int vertex)
{
    if (used[vertex])
    {
        return false;
    }
    used[vertex] = true;
    for (int next : adjList[vertex])
    {
        if (r[next] == -1 || dfs(r[next]))
        {
            r[next] = vertex;
            l[vertex] = next;
            return true;
        }
    }
    return false;
}

int main()
{
    std::ifstream in("matching.in");
    std::ofstream out("matching.out");

    int n;
    in >> n;

    std::vector<Vertex> vertecies;
    for (int i = 0; i < n; ++i)
    {
        int w;
        in >> w;
        vertecies.push_back(Vertex(i, w));
    }
    std::sort(vertecies.begin(), vertecies.end(), sortByWeight);

    adjList.resize(n);
    for (int i = 0; i < n; ++i)
    {
        int size;
        in >> size;

        for (int j = 0; j < size; ++j)
        {
            int vertex;
            in >> vertex;
            adjList[i].push_back(vertex - 1);
        }
    }

    l.resize(n, -1);
    r.resize(n, -1);
    for (Vertex v : vertecies)
    {
        used.assign(n, false);
        dfs(v.index);
    }

    for (int vertex : l)
    {
        out << vertex + 1 << ' ';
    }
}