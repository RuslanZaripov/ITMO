#include <iostream>
#include <vector>
#include <queue>

int main()
{
    std::queue<int> pruferCode;

    int n;
    std::cin >> n;
    std::vector<int> nodeDegree(n, 1);
    for (int i = 0; i < n - 2; ++i)
    {
        int number;
        std::cin >> number;
        number -= 1;
        pruferCode.push(number);
        nodeDegree[number] += 1;
    }
    std::priority_queue<int, std::vector<int>, std::greater<int>> leavesList;
    for (int i = 0; i < n; ++i)
    {
        if (nodeDegree[i] == 1)
        {
            leavesList.push(i);
        }
    }

    for (int i = 0; i < n - 2; ++i)
    {
        int minimalLeaf = leavesList.top();
        leavesList.pop();

        int parentNode = pruferCode.front();
        pruferCode.pop();

        std::cout << minimalLeaf + 1 << " " << parentNode + 1 << std::endl;
        nodeDegree[minimalLeaf] -= 1;
        nodeDegree[parentNode] -= 1;
        if (nodeDegree[parentNode] == 1)
        {
            leavesList.push(parentNode);
        }
    }

    for (int node = 0; node < n; ++node)
    {
        if (nodeDegree[node] == 1)
        {
            std::cout << node + 1 << " ";
        }
    }
}