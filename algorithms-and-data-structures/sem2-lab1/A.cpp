#include <iostream>
#include <vector>
#include <cmath>
using namespace std;

vector <long long> tree;

void set(int i, long long v, int x, int lx, int rx)
{
    if (rx - lx == 1)
        tree[x] = v;
    else
    {
        int m = (lx + rx) / 2;
        if (i < m)
        {
            set(i, v, 2 * x + 1, lx, m);
        }
        else
        {
            set(i, v, 2 * x + 2, m, rx);
        }
        tree[x] = tree[2 * x + 1] + tree[2 * x + 2];
    }
}
long long sum(int l, int r, int x, int lx, int rx)
{
    if (l >= rx || lx >= r)
        return 0;
    if (lx >= l && rx <= r)
        return tree[x];
    int m = (lx + rx) / 2;
    return sum(l, r, 2 * x + 1, lx, m) + sum(l, r, 2 * x + 2, m, rx);
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
    tree.resize(2 * capacity - 1, 0);
    for (int i = 0; i < n; i++)
    {
        cin >> tree[capacity - 1 + i];
    }
    for (int i = capacity - 2; i >= 0; i--)
    {
        tree[i] = tree[2 * i + 1] + tree[2 * i + 2];
    }
    for (int i = 0; i < k; i++)
    {
        int op;
        cin >> op;
        if (op == 1)
        {
            int l;
            long long v;
            cin >> l >> v;
            set(l, v, 0, 0, capacity);
        }
        else if (op == 2)
        {
            int l, r;
            cin >> l >> r;
            cout << sum(l, r, 0, 0, capacity) << endl;
        }
    }
}