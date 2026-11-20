# Git cheatsheet

## Useful commands:

* Updating changes after gitignore update:

```git rm -r --cached .```
```git add .```

* Overwrite the master branch with the latest state of another branch.

```git switch master```
```git reset --hard feature```
```git push --force origin master```

