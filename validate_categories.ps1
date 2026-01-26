$connString = "Server=44.193.11.135;Database=SignusID_Migracion;User Id=ActiveID_Imas;Password=SetDiv2023;Encrypt=False;TrustServerCertificate=True;"
$conn = New-Object System.Data.SqlClient.SqlConnection($connString)
try {
    $conn.Open()
    $cmd = $conn.CreateCommand()
    
    # 1. Total Count
    $cmd.CommandText = "SELECT COUNT(*) FROM ActivosSignusID"
    $total = $cmd.ExecuteScalar()
    Write-Output "Total Assets: $total"

    # 2. Count NULL Category
    $cmd.CommandText = "SELECT COUNT(*) FROM ActivosSignusID WHERE CATEGORIA IS NULL"
    $nullCat = $cmd.ExecuteScalar()
    Write-Output "Assets with NULL Category: $nullCat"

    # 3. Count Empty GUID Category
    $cmd.CommandText = "SELECT COUNT(*) FROM ActivosSignusID WHERE CATEGORIA = '00000000-0000-0000-0000-000000000000'"
    $emptyCat = $cmd.ExecuteScalar()
    Write-Output "Assets with Empty GUID Category: $emptyCat"

    # 4. Group by Category to see distribution
    $cmd.CommandText = "SELECT TOP 10 CATEGORIA, COUNT(*) as Cnt FROM ActivosSignusID GROUP BY CATEGORIA ORDER BY Cnt DESC"
    $reader = $cmd.ExecuteReader()
    Write-Output "`nTop Categories:"
    while ($reader.Read()) {
        $cat = if ($reader["CATEGORIA"] -is [DBNull]) { "NULL" } else { $reader["CATEGORIA"] }
        Write-Output "Category: $cat - Count: $($reader['Cnt'])"
    }

} catch {
    Write-Error $_
} finally {
    if ($conn.State -eq 'Open') { $conn.Close() }
}
