#include <iostream>
#include <vector>

int main()
{
    int n;
    std::cin >> n;

    std::vector<int> sortedLampList;

    sortedLampList.push_back(1);

    for (int lamp = 2; lamp <= n; ++lamp)
    {
        int l = -1, r = sortedLampList.size();
        while (l < r - 1)
        {
            int m = (l + r) >> 1;
            std::cout << "1 " << sortedLampList[m] << " " << lamp << std::endl;
            std::cout.flush();

            std::string answer;
            std::cin >> answer;
            answer == "YES" ? l = m : r = m;
        }
        sortedLampList.insert(sortedLampList.begin() + r, lamp);
    }

    std::cout << "0 ";
    for (int lamp : sortedLampList)
    {
        std::cout << lamp << " ";
    }
    std::cout << std::endl;
    std::cout.flush();
}