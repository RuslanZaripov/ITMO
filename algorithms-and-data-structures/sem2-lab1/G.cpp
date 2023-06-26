#include <iostream>
#include <vector>
#include <cmath>
const long long INF = 1e10;
using namespace std;

vector<pair<long long, long long>> tree;

void print()
{
    cout << "Tree:" << endl;
    for (int i = 1; i < tree.size(); i *= 2)
    {
        cout << "> ";
        for (int j = i; j < 2 * i; j++)
        {
            cout << "(";
            if (tree[j - 1].first == INF)
            {
                cout << "INF";
            }
            else
            {
                cout << tree[j - 1].first;
            }
            cout << "|" << tree[j - 1].second << ")";
        }
        cout << endl;
    }
}
pair<long long, long long> upd(pair<long long, long long> a, pair<long long, long long> b)
{
    if (a.second != -1 && b.second != -1)
    {
        return make_pair(min(a.second, b.second), -1);
    }
    else
    {
        if (a.second >= 0)
        {
            return make_pair(min(a.second, b.first), -1);
        }
        else if (b.second >= 0)
        {
            return make_pair(min(b.second, a.first), -1);
        }
    }
    return make_pair(min(a.first, b.first), -1);
}
void fpropogate(int x)
{
    if (tree[x].second != -1)
    {
        tree[2 * x + 1].second = tree[x].second;
        tree[2 * x + 2].second = tree[x].second;
        tree[x].second = -1;
    }
}
long long tmin(int l, int r, int x, int lx, int rx)
{
    int left = 2 * x + 1;
    int right = 2 * x + 2;
    if (lx >= r || l >= rx)
        return INF;
    if (lx >= l && rx <= r)
    {
        if (tree[x].second == -1)
        {
            return tree[x].first;
        }
        else
        {
            return tree[x].second;
        }
    }
    fpropogate(x);
    while (left != 0)
    {
        left = (left - 1) / 2;
        tree[left] = upd(tree[2 * left + 1], tree[2 * left + 2]);
    }
    int mx = (lx + rx) / 2;
    // int result = min(tmin(l, r, 2 * x + 1, lx, mx), tmin(l, r, 2 * x + 2, mx, rx));
    // cout << " result:  " << lx << ", " << rx << ": " << result << endl;
    return min(tmin(l, r, 2 * x + 1, lx, mx), tmin(l, r, 2 * x + 2, mx, rx));
}
void big_set(int l, int r, long long v, int x, int lx, int rx)
{
    int left = 2 * x + 1;
    int right = 2 * x + 2;
    if (lx >= r || l >= rx)
    {
        return;
    }
    if (lx >= l && rx <= r)
    {
        tree[x].second = v;
        return;
    }
    fpropogate(x);
    int m = (lx + rx) / 2;
    big_set(l, r, v, left, lx, m);
    big_set(l, r, v, right, m, rx);
    while (left != 0)
    {
        left = (left - 1) / 2;
        tree[left] = upd(tree[2 * left + 1], tree[2 * left + 2]);
    }
}
int main()
{
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);
    int n, m;
    cin >> n >> m;
    int capacity = 1;
    while (capacity < n)
    {
        capacity *= 2;
    }
    tree.resize(2 * capacity - 1, make_pair(INF, -1));
    for (int i = 0; i < n; i++)
    {
        tree[capacity - 1 + i].first = 0;
    }
    for (int i = capacity - 2; i >= 0; i--)
    {
        tree[i].first = min(tree[2 * i + 1].first, tree[2 * i + 2].first);
    }
    for (int i = 0; i < m; i++)
    {
        int op;
        cin >> op;
        if (op == 1)
        {
            int l, r;
            long long v;
            cin >> l >> r >> v;
            big_set(l, r, v, 0, 0, capacity);
            // print();
        }
        else if (op == 2)
        {
            int l, r;
            cin >> l >> r;
            cout << tmin(l, r, 0, 0, capacity) << endl;
            // print();
        }
    }
}