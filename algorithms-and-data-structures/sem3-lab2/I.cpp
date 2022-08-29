#include <iostream>
#include <vector>

const std::string WIN = "WIN";
const std::string LOSE = "LOSE";
const std::string DRAW = "DRAW";

std::vector<std::vector<int>> inversedAdjList;
std::vector<std::vector<int>> adjList;
std::vector<std::string> wins;
std::vector<bool> visited;

void dfs(int node)
{
    visited[node] = true;
    for (int nextNode : inversedAdjList[node])
    {
        if (!visited[nextNode])
        {
            if (wins[node] == LOSE)
            {
                wins[nextNode] = WIN;
            }
            else if (adjList[nextNode].size() == 1)
            {
                wins[nextNode] = LOSE;
            }
            else
            {
                continue;
            }
            dfs(nextNode);
        }
    }
}

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n, m;
    while (std::cin >> n >> m)
    {
        inversedAdjList.assign(n, std::vector<int>());
        adjList.assign(n, std::vector<int>());

        for (int i = 0; i < m; ++i)
        {
            int start, end;
            std::cin >> start >> end;
            start -= 1;
            end -= 1;
            inversedAdjList[end].push_back(start);
            adjList[start].push_back(end);
        }

        wins.assign(n, DRAW);
        visited.assign(n, false);

        for (int node = 0; node < n; ++node)
        {
            if (adjList[node].empty())
            {
                wins[node] = LOSE;
                dfs(node);
            }
        }

        for (std::string result : wins)
        {
            if (result == WIN)
            {
                std::cout << "FIRST";
            }
            else if (result == LOSE)
            {
                std::cout << "SECOND";
            }
            else if (result == DRAW)
            {
                std::cout << DRAW;
            }
            std::cout << '\n';
        }
        std::cout << '\n';
    }
}