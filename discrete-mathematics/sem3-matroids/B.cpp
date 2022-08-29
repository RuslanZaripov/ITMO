#include <iostream>
#include <vector>
#include <algorithm>
#include <ostream>
#include <fstream>

std::vector<long> rank;
std::vector<long> parent;

long find(long &v)
{
    if (v == parent[v])
    {
        return v;
    }
    return parent[v] = find(parent[v]);
}

void unite(long &a, long &b)
{
    a = find(a);
    b = find(b);
    if (a == b)
    {
        return;
    }
    if (rank[a] < rank[b])
    {
        std::swap(a, b);
    }
    parent[b] = a;
    if (rank[a] == rank[b])
    {
        ++rank[a];
    }
}

struct Edge
{
    long index;
    long from, to;
    double weight;
    Edge(long m_index, long m_from, long m_to, double m_weight)
        : index(m_index),
          from(m_from),
          to(m_to),
          weight(m_weight) {}

    friend std::ostream &operator<<(std::ostream &os, const Edge &edge)
    {
        return os << edge.from + 1 << ' ' << edge.to + 1 << ' ' << edge.weight;
    }
};

bool sortEdgesByWeight(const Edge &a, const Edge &b)
{
    return a.weight > b.weight;
}

bool sortEdgesByIndex(const Edge &a, const Edge &b)
{
    return a.index < b.index;
}

int main()
{
    std::ifstream in("destroy.in");
    std::ofstream out("destroy.out");

    long n, m;
    double s;

    in >> n >> m >> s;

    rank.resize(n, 0);
    parent.resize(n, 0);
    for (long i = 0L; i < n; ++i)
    {
        parent[i] = i;
    }

    std::vector<Edge> edges;
    for (long i = 0L; i < m; ++i)
    {
        long from, to;
        double weight;
        in >> from >> to >> weight;

        from = from - 1;
        to = to - 1;

        edges.push_back(Edge(i + 1, from, to, weight));
    }

    std::sort(edges.begin(), edges.end(), sortEdgesByWeight);

    std::vector<Edge> specialEdges;
    for (Edge edge : edges)
    {
        if (find(edge.from) != find(edge.to))
        {
            unite(edge.from, edge.to);
        }
        else
        {
            specialEdges.push_back(edge);
        }
    }
    std::reverse(specialEdges.begin(), specialEdges.end());

    std::vector<Edge> ans;
    double sum = 0.0;
    for (Edge edge : specialEdges)
    {
        if (sum + edge.weight > s)
        {
            break;
        }
        else
        {
            sum += edge.weight;
            ans.push_back(edge);
        }
    }

    std::sort(ans.begin(), ans.end(), sortEdgesByIndex);

    out << ans.size() << '\n';
    for (Edge edge : ans)
    {
        out << edge.index << ' ';
    }
}