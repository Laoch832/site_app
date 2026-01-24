import time
import subprocess
import sys
import json
import os

def run_command(command):
    try:
        result = subprocess.run(command, shell=True, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        return result.stdout.strip()
    except subprocess.CalledProcessError as e:
        print(f"Error running command: {command}")
        print(e.stderr)
        return None

def get_latest_run():
    print("Fetching latest workflow run...")
    # Get the latest run for the 'android_release.yml' workflow
    cmd = "gh run list --workflow android_release.yml --limit 1 --json databaseId,status,conclusion,url"
    output = run_command(cmd)
    if not output:
        return None
    runs = json.loads(output)
    if not runs:
        print("No runs found.")
        return None
    return runs[0]

def monitor_run(run_id):
    print(f"Monitoring run ID: {run_id}")
    while True:
        cmd = f"gh run view {run_id} --json status,conclusion"
        output = run_command(cmd)
        if not output:
            break
        
        data = json.loads(output)
        status = data.get("status")
        conclusion = data.get("conclusion")
        
        print(f"Status: {status}, Conclusion: {conclusion}")
        
        if status == "completed":
            return conclusion
        
        time.sleep(10)

def handle_success(run_id):
    print("\nBuild Successful! Downloading release...")
    # Get the release tag associated with this run? 
    # Or just get the latest release
    cmd = "gh release download --pattern \"*.apk\" --dir release_downloads"
    output = run_command(cmd)
    if output:
        print(output)
    print("Download complete. Check 'release_downloads' directory.")

def handle_failure(run_id):
    print("\nBuild Failed! Fetching failure reason...")
    cmd = f"gh run view {run_id} --log-failed"
    output = run_command(cmd)
    if output:
        print("\n--- FAILURE LOGS ---\n")
        print(output)
        print("\n--------------------\n")

def main():
    # Check if gh is installed
    if not run_command("gh --version"):
        print("GitHub CLI (gh) is not installed or not in PATH. Please install it to use this script.")
        return

    print("Checking for latest run...")
    run = get_latest_run()
    if not run:
        return

    run_id = run['databaseId']
    conclusion = run['conclusion']
    status = run['status']

    if status != "completed":
        conclusion = monitor_run(run_id)

    if conclusion == "success":
        handle_success(run_id)
    else:
        handle_failure(run_id)

if __name__ == "__main__":
    main()
