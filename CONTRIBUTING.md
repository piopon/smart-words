# Contributing to the **smart-words** repo

I really appreciate all contributions via GitHub pull requests.
To contribute take the following steps:

## Forking and cloning Smart Words

- Visit https://github.com/piopon/smart-words
- Click the "Fork" button to make a copy of the repository under your own GitHub account.
- Navigate to your copy of the repository and click the green "Clone or download" button.
- Copy the address and clone it to your local machine
  ```sh
     git clone <the pasted URL>
  ```

## Making changes locally

- Make sure you are up to date with the latest code on the master and checkout to your branch
  ```sh
     git fetch upstream
     git checkout upstream/master -b <name_of_your_branch>
  ```     
- Apply and verify your changes

  Keep your changes as minimal as possible to fix a specified issue.<br>
  Fix only one issue in a single branch, otherwise your request may be rejected!
- Commit your changes adding a clear and concise commit message
   ```sh
     git commit -m "<your informative commit message>"
   ```
 - Push changes to your fork:
   ```sh
     git push origin <name_of_your_branch>
   ```

## Submitting pull request on GitHub

- Go to https://github.com/piopon/smart-words
- You should see a big green button marked "Compare and pull request"
- Click that button. You should see a page giving you the opportunity to open a pull request
- **Review your changes** 
- Write a clear and concise subject and description for your change. If your change is related to GUI please add a screenshot with your changes to speed up review/discussion process.
- Click the green "Create pull request" button

The rest of the procedure is the same as for editing a file directly on GitHub, the change is reviewed for conflicts against the base branch, the owner gets an email notification, and if happy will merge your change or start a review process.
