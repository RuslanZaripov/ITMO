#include <iostream>
#include <vector>
#include <cmath>
using namespace std;

vector<int> tree;

void set(int i, int x, int lx, int rx)
{
    if (rx - lx == 1)
    {
        if (tree[x] == 0)
        {
            tree[x] = 1;
        }
        else
        {
            tree[x] = 0;
        }
    }
    else
    {
        int m = (lx + rx) / 2;
        if (i < m)
        {
            set(i, 2 * x + 1, lx, m);
        }
        else
        {
            set(i, 2 * x + 2, m, rx);
        }
        tree[x] = tree[2 * x + 1] + tree[2 * x + 2];
    }
}
int find(int k, int x, int lx, int rx)
{
    if (rx - lx == 1)
        return lx;
    int left = 2 * x + 1;
    int right = 2 * x + 2;
    int m = (lx + rx) / 2;
    if (tree[left] < k)
        return find(k - tree[left], right, m, rx);
    else
        return find(k, left, lx, m);
}
int main()
{
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
    int n, k;
    cin >> n >> k;
    int capacity = 1;
    while (capacity < n)
    {
        capacity *= 2;
    }
    tree.resize(2 * capacity - 1);
    for (int i = 0; i < n; i++)
    {
        cin >> tree[capacity - 1 + i];
    }
    for (int i = capacity - 2; i >= 0; i--)
    {
        tree[i] = tree[2 * i + 1] + tree[2 * i + 2];
    }
    // for (int i = 0; i < tree.size(); i++)
    // {
    //     cout << tree[i] << " ";
    // }
    // cout << endl;
    for (int i = 0; i < k; i++)
    {
        int op;
        cin >> op;
        if (op == 1)
        {
            int j;
            cin >> j;
            set(j, 0, 0, capacity);
            // for (int i = 0; i < tree.size(); i++)
            // {
            //     cout << i << ": " << tree[i] << endl;
            // }
        }
        else if (op == 2)
        {
            int v;
            cin >> v;
            cout << find(v + 1, 0, 0, capacity) << endl;
        }
    }
}