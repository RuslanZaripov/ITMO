#include <iostream>
#include <vector>
#include <map>

std::vector<bool> used;
std::vector<int> matching;
std::map<int, std::vector<int>> adjList;
std::vector<std::pair<int, int>> moves = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

bool dfs(int vertex)
{
    if (used[vertex])
    {
        return false;
    }
    used[vertex] = true;
    for (int next : adjList[vertex])
    {
        if (matching[next] == -1 || dfs(matching[next]))
        {
            matching[next] = vertex;
            return true;
        }
    }
    return false;
}

bool isValid(int i, int j, int n, int m)
{
    return (0 <= i && i < n) && (0 <= j && j < m);
}

int getCellNumber(int i, int j, int m)
{
    return i * m + j;
}

bool dividePartites(int i, int j)
{
    return (i + j) % 2;
}

int main()
{
    std::ios::sync_with_stdio(false);
    std::cin.tie(nullptr);

    int n, m, a, b;
    std::cin >> n >> m >> a >> b;
    int cellsAmount = n * m;
    int freeCellsAmount = 0;

    std::vector<std::vector<bool>> isEmpty(n, std::vector<bool>(m));

    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < m; j++)
        {
            char c;
            std::cin >> c;

            isEmpty[i][j] = (c == '*' ? true : false);
            freeCellsAmount += (isEmpty[i][j] ? 1 : 0);
        }
    }

    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < m; j++)
        {
            if (isEmpty[i][j] && dividePartites(i, j))
            {
                for (std::pair<int, int> move : moves)
                {
                    int next_j = j + move.first;
                    int next_i = i + move.second;

                    if (isValid(next_i, next_j, n, m) && isEmpty[next_i][next_j])
                    {
                        adjList[getCellNumber(i, j, m)].push_back(getCellNumber(next_i, next_j, m));
                    }
                }
            }
        }
    }

    matching.resize(cellsAmount, -1);
    for (int cell = 0; cell < cellsAmount; ++cell)
    {
        used.assign(cellsAmount, false);
        dfs(cell);
    }

    int dominoAmount = 0;
    for (int cell = 0; cell < cellsAmount; ++cell)
    {
        if (matching[cell] != -1)
        {
            dominoAmount += 1;
        }
    }

    std::cout << std::min(freeCellsAmount * b, dominoAmount * a + (freeCellsAmount - 2 * dominoAmount) * b) << std::endl;
}