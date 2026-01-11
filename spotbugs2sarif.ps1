param (
    [string]$InputXml = "target/spotbugsXml.xml",
    [string]$OutputJson = "gl-sast-report.json"
)

# Load XML
[xml]$xml = Get-Content $InputXml

# Prepare SARIF structure
$sarif = @{
    version = "2.1.0"
    runs = @(@{
        tool = @{
            driver = @{
                name = "SpotBugs"
            }
        }
        results = @()
    })
}

foreach ($bug in $xml.BugCollection.BugInstance) {
    $result = @{
        ruleId = $bug.type
        level  = "warning"
        message = @{
            text = $bug.ShortMessage
        }
    }
    $sarif.runs[0].results += $result
}

# Save as JSON
$sarif | ConvertTo-Json -Depth 5 | Out-File -Encoding UTF8 $OutputJson

Write-Host "SARIF report generated: $OutputJson"
