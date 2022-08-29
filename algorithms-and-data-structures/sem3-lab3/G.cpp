#include <iostream>
#include <vector>
#include <queue>
#include <set>
#include <sstream>
#include <map>

const int alphabet = 26;

struct Node
{
    std::vector<Node*> child;
    Node* suff;
    bool isTerminal;
    std::vector<int> index;
    Node() : isTerminal(false), suff(nullptr)
    {
        child.resize(alphabet, nullptr);
    }
};

std::map<int, Node*> terminals;
std::map<Node*, bool> used;

Node* root = new Node();

// Node *add(Node *v, std::string &s, int i, int index)
// {
//     if (!v)
//     {
//         v = new Node();
//     }
//     if (i == s.size())
//     {
//         v->isTerminal = true;
//         v->index.push_back(index);
//         return v;
//     }
//     int ch = s[i] - 'a';
//     v->child[ch] = add(v->child[ch], s, i + 1, index);
//     return v;
// }

void add(std::string& str, int strIndex)
{
    Node* curr = root;
    for (int j = 0; j < str.size(); ++j)
    {
        int ch = str[j] - 'a';
        if (!curr->child[ch])
        {
            curr->child[ch] = new Node();
        }
        curr = curr->child[ch];
        if (str.size() - j == 1)
        {
            curr->isTerminal = true;
            curr->index.push_back(strIndex);
            terminals[strIndex] = curr;
        };
    }
}

void buildLinks()
{
    Node* f = new Node();
    f->child.assign(alphabet, root);
    root->suff = f;

    std::queue<Node*> q;
    q.push(root);
    while (!q.empty())
    {
        Node* v = q.front();
        q.pop();

        for (int ch = 0; ch < alphabet; ++ch)
        {
            if (!v->child[ch])
            {
                v->child[ch] = v->suff->child[ch];
            }
            else
            {
                Node* next = v->child[ch];
                next->suff = v->suff->child[ch];
                if (next->suff->isTerminal)
                {
                    next->isTerminal = next->suff->isTerminal;
                }
                q.push(next);
            }
        }
    }
}

int main()
{
    std::ios_base::sync_with_stdio(0);
    std::cin.tie(0);
    std::cout.tie(0);

    int k;
    std::cin >> k;

    for (int strIndex = 0; strIndex < k; ++strIndex)
    {
        std::string str;
        std::cin >> str;
        add(str, strIndex);
        // root = add(root, str, 0, i + 1);
    }
    buildLinks();

    std::string t;
    std::cin >> t;

    Node* curr = root;
    used[curr] = true;

    for (auto& ch : t)
    {
        curr = curr->child[ch - 'a'];
        used[curr] = true;
        if (curr->isTerminal)
        {
            Node* tmp = curr;
            while (tmp->suff->isTerminal && !used[tmp->suff])
            {
                tmp = tmp->suff;
                used[tmp] = true;
            }
        }
    }

    std::stringstream ss;
    for (int i = 0; i < k; ++i)
    {
        ss << (used[terminals[i]] ? "YES" : "NO") << "\n";
    }
    std::cout << ss.str();
}