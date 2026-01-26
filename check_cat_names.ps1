$connString = "Server=44.193.11.135;Database=SignusID_Migracion;User Id=ActiveID_Imas;Password=SetDiv2023;Encrypt=False;TrustServerCertificate=True;"
$conn = New-Object System.Data.SqlClient.SqlConnection($connString)
try {
    $conn.Open()
    $cmd = $conn.CreateCommand()
    $cmd.CommandText = "SELECT assetCategorySysId, name FROM assetCategory WHERE assetCategorySysId IN ('74de6a97-4a32-4f64-a102-93ae2177ac5a', 'f5ea7480-41de-4e5c-8528-650bef1dd9aa', '851ba166-8591-4340-ba2c-6493ac078413')"
    $reader = $cmd.ExecuteReader()
    while ($reader.Read()) {
        Write-Output "ID: $($reader['assetCategorySysId']) - Name: $($reader['name'])"
    }
} catch {
    Write-Error $_
} finally {
    if ($conn.State -eq 'Open') { $conn.Close() }
}
