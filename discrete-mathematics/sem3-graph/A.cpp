#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
#include <deque>

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n;
    std::cin >> n;

    std::vector<std::vector<int>> adjMatrix(n, std::vector<int>(n, 0));
    std::deque<int> vertexQueue;
    for (int i = 1; i < n; ++i)
    {
        std::string input;
        std::cin >> input;
        for (int j = 0; j < input.size(); ++j)
        {
            if (input[j] == '1')
            {
                adjMatrix[i][j] = adjMatrix[j][i] = 1;
            }
        }
    }

    for (int i = 0; i < n; ++i)
    {
        vertexQueue.push_back(i);
    }
    for (int k = 0; k < n * (n - 1); ++k)
    {
        if (!adjMatrix[vertexQueue[0]][vertexQueue[1]])
        {
            int i = 2;
            while (!adjMatrix[vertexQueue[0]][vertexQueue[i]] || !adjMatrix[vertexQueue[1]][vertexQueue[i + 1]])
            {
                i++;
            }
            std::reverse(vertexQueue.begin() + 1, vertexQueue.begin() + i + 1);
        }
        vertexQueue.push_back(vertexQueue.front());
        vertexQueue.pop_front();
    }

    for (int i = 0; i < vertexQueue.size(); ++i)
    {
        std::cout << vertexQueue[i] + 1 << " ";
    }
}