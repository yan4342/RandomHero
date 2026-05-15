# Extract missing heroes information
$jsFile = "d:\Android\AndroidStudioProjects\random\herolist.js"
$missingHeroes = @(151,155,159,172,176,179,188,505,506,508,509,510,511,514,517,519,521,524,527,528,529,531,533,534,536,537,538,540,542,544,545,548,550,558,563,564,577,581,582,583,584,585)

$content = Get-Content $jsFile -Raw

$kotlinEntries = @()

foreach ($heroId in $missingHeroes) {
    # Find hero information
    $pattern = "\{\s*""ename"": $heroId,[\s\S]*?\},"
    if ($content -match $pattern) {
        $heroBlock = $matches[0]
        
        # Extract fields
        $cname = if ($heroBlock -match '"cname": "([^"]+)"') { $matches[1] } else { "" }
        $id_name = if ($heroBlock -match '"id_name": "([^"]+)"') { $matches[1] } else { "" }
        $title = if ($heroBlock -match '"title": "([^"]+)"') { $matches[1] } else { "" }
        $new_type = if ($heroBlock -match '"new_type": (\d+)') { $matches[1] } else { "0" }
        $hero_type = if ($heroBlock -match '"hero_type": (\d+)') { $matches[1] } else { "0" }
        $hero_type2 = if ($heroBlock -match '"hero_type2": (\d+)') { $matches[1] } else { $null }
        $skin_name = if ($heroBlock -match '"skin_name": "([^"]+)"') { $matches[1] } else { "" }
        $moss_id = if ($heroBlock -match '"moss_id": (\d+)') { $matches[1] } else { "0" }
        $pay_type = if ($heroBlock -match '"pay_type": (\d+)') { $matches[1] } else { $null }
        
        # Build Kotlin entry
        $heroType2Param = if ($hero_type2) { ", $hero_type2" } else { ", null" }
        $payTypeParam = if ($pay_type) { ", $pay_type" } else { "" }
        
        $kotlinEntry = "        Hero($heroId, `"$cname`", `"$id_name`", `"$title`", $new_type, $hero_type$heroType2Param, `"$skin_name`", $moss_id$payTypeParam)"
        $kotlinEntries += $kotlinEntry
        
        Write-Host "Extracted hero $heroId: $cname"
    } else {
        Write-Host "Hero $heroId not found"
    }
}

# Output results
Write-Host "`nGenerated Kotlin entries:"
foreach ($entry in $kotlinEntries) {
    Write-Host $entry
}

# Save to file
$outputFile = "d:\Android\AndroidStudioProjects\random\missing_heroes.txt"
$kotlinEntries | Out-File -FilePath $outputFile -Encoding UTF8
Write-Host "`nSaved to: $outputFile"