#include <iostream>
#include <vector>
#include <map>
#include <set>

int k, n, m;

std::vector<bool> isInvitedBoy;
std::vector<bool> isInvitedGirl;

std::vector<bool> used;
std::map<int, std::vector<int>> adjList;
std::vector<int> matching;

bool kuhn(int vertex)
{
    if (used[vertex])
    {
        return false;
    }
    used[vertex] = true;
    for (int next : adjList[vertex])
    {
        if (matching[next] == -1 || kuhn(matching[next]))
        {
            matching[next] = vertex;
            return true;
        }
    }
    return false;
}

void dfs(int boy)
{
    if (isInvitedBoy[boy])
    {
        return;
    }
    isInvitedBoy[boy] = true;
    for (int girl : adjList[boy])
    {
        isInvitedGirl[girl] = false;
        if (matching[girl] != -1)
        {
            dfs(matching[girl]);
        }
    }
}

void printAnswer(std::vector<int>& invitedBoys, std::vector<int>& invitedGirls)
{
    std::cout << invitedGirls.size() + invitedBoys.size() << '\n';
    std::cout << invitedBoys.size() << ' ' << invitedGirls.size() << '\n';
    for (int boy : invitedBoys)
    {
        std::cout << boy + 1 << ' ';
    }
    std::cout << '\n';
    for (int girl : invitedGirls)
    {
        std::cout << girl + 1 << ' ';
    }
    std::cout << '\n';
}

void collect(std::vector<int>& invitedBoys, std::vector<int>& invitedGirls)
{
    for (int boy = 0; boy < m; ++boy)
    {
        if (isInvitedBoy[boy])
        {
            invitedBoys.push_back(boy);
        }
    }

    for (int girl = 0; girl < n; ++girl)
    {
        if (isInvitedGirl[girl])
        {
            invitedGirls.push_back(girl);
        }
    }
}

int main()
{
    std::ios::sync_with_stdio(false);
    std::cin.tie(nullptr);

    std::cin >> k;
    for (int i = 0; i < k; i++)
    {
        std::cin >> m >> n;

        for (int boy = 0; boy < m; boy++)
        {
            std::set<int> knownGirls;

            int girl;
            std::cin >> girl;
            while (girl != 0)
            {
                knownGirls.insert(girl - 1);
                std::cin >> girl;
            }

            for (int girl = 0; girl < n; girl++)
            {
                if (!knownGirls.count(girl))
                {
                    adjList[boy].push_back(girl);
                }
            }
        }

        matching.assign(n, -1);
        for (int boy = 0; boy < m; ++boy)
        {
            used.assign(m, false);
            kuhn(boy);
        }

        std::vector<bool> isFree(m, true);
        for (int girl = 0; girl < n; ++girl)
        {
            if (matching[girl] != -1)
            {
                isFree[matching[girl]] = false;
            }
        }

        isInvitedGirl.assign(n, true);
        isInvitedBoy.assign(m, false);
        for (int boy = 0; boy < m; ++boy)
        {
            if (isFree[boy] && !isInvitedBoy[boy])
            {
                dfs(boy);
            }
        }

        std::vector<int> invitedBoys;
        std::vector<int> invitedGirls;
        collect(invitedBoys, invitedGirls);
        printAnswer(invitedBoys, invitedGirls);

        adjList.clear();
    }
}