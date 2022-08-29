#include <iostream>
#include <vector>
#include <set>
#include <queue>

struct CompareVertexData
{
    bool operator()(std::pair<int, int> v1, std::pair<int, int> v2)
    {
        return v1.second > v2.second;
    }
};

int main()
{
    int n, m;
    std::cin >> n >> m;

    std::vector<std::set<std::pair<int, int>>> adjList(n);
    std::vector<int> visited(n);
    for (int i = 0; i < m; ++i)
    {
        int begin, end, weight;
        std::cin >> begin >> end >> weight;
        begin = begin - 1;
        end = end - 1;
        adjList[begin].insert({ end, weight });
        adjList[end].insert({ begin, weight });
    }

    std::priority_queue<std::pair<int, int>,
        std::vector<std::pair<int, int>>,
        CompareVertexData>
        vertexQueue;

    vertexQueue.push({ 0, 0 });
    long long mstMinWeight = 0;
    while (!vertexQueue.empty())
    {
        std::pair<int, int> vertexData = vertexQueue.top();
        vertexQueue.pop();

        int vertex = vertexData.first;
        int dist = vertexData.second;
        if (!visited[vertex])
        {
            visited[vertex] = true;
            mstMinWeight += dist;

            for (std::pair<int, int> nextVertexData : adjList[vertex])
            {
                int nextNode = nextVertexData.first;
                if (!visited[nextNode])
                {
                    vertexQueue.push({ nextNode, nextVertexData.second });
                }
            }
        }
    }

    std::cout << mstMinWeight << std::endl;
}