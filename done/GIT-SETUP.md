# Git Setup with Multiple SSH Keys

## Current Configuration

This repository is configured to use your second SSH key (`id_ed25519_campusut`) for GitHub authentication.

### SSH Config (~/.ssh/config)

```ssh
# Default GitHub account
Host github.com
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519

# Second GitHub account (campusut)
Host github-campusut
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519_campusut
  IdentitiesOnly yes
```

### Remote URL

```bash
git remote -v
# origin  git@github-campusut:044720182/neurixa.git (fetch)
# origin  git@github-campusut:044720182/neurixa.git (push)
```

## Common Git Commands

### Push changes
```bash
git add .
git commit -m "Your commit message"
git push origin main
```

### Pull changes
```bash
git pull origin main
```

### Check status
```bash
git status
```

### View commit history
```bash
git log --oneline -10
```

## Troubleshooting

### If SSH key is not loaded
```bash
ssh-add ~/.ssh/id_ed25519_campusut
```

### Test SSH connection
```bash
ssh -T git@github-campusut
# Should show: Hi 044720182! You've successfully authenticated...
```

### If you get permission denied
```bash
# Check which keys are loaded
ssh-add -l

# Add your key
ssh-add ~/.ssh/id_ed25519_campusut

# Test again
ssh -T git@github-campusut
```

### Switch to different SSH key
If you want to use a different key for this repo:

```bash
# Update remote URL
git remote set-url origin git@github.com:username/repo.git

# Or for another custom host
git remote set-url origin git@github-other:username/repo.git
```

## Repository Information

- **GitHub Account**: 044720182
- **Repository**: neurixa
- **SSH Key**: id_ed25519_campusut
- **Email**: 044720182@ecampus.ut.ac.id

## Initial Push (Already Done)

```bash
✅ Repository initialized
✅ SSH key configured
✅ Remote added: git@github-campusut:044720182/neurixa.git
✅ Code pushed to main branch
```

## Next Steps

1. Continue developing features
2. Commit regularly with meaningful messages
3. Push to GitHub: `git push origin main`
4. Create branches for new features: `git checkout -b feature/your-feature`
5. Merge branches: `git checkout main && git merge feature/your-feature`
