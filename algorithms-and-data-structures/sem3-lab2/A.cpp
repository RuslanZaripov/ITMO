#include <iostream>
#include <vector>

void print(std::vector<std::vector<int>>& matrix)
{
    std::cout << '\n';

    int size = matrix.size();
    for (int i = 0; i < size; ++i)
    {
        for (int j = 0; j < size; ++j)
        {
            std::cout << matrix[i][j] << " ";
        }
        std::cout << '\n';
    }
}

int main()
{
    int n;
    std::cin >> n;
    std::vector<std::vector<int>> distMatrix(n, std::vector<int>(n, 0));
    for (int i = 0; i < n; ++i)
    {
        for (int j = 0; j < n; ++j)
        {
            std::cin >> distMatrix[i][j];
        }
    }

    for (int k = 0; k < n; ++k)
    {
        for (int i = 0; i < n; ++i)
        {
            for (int j = 0; j < n; ++j)
            {
                if (distMatrix[i][k] + distMatrix[k][j] < distMatrix[i][j])
                {
                    distMatrix[i][j] = distMatrix[i][k] + distMatrix[k][j];
                }
            }
        }
    }

    print(distMatrix);
}