<?php
// REGISTRO: Recibe datos de CrearPerfilActivity, encripta la clave con BCrypt y guarda el usuario en MySQL.
header('Content-Type: application/json; charset=UTF-8');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

// ESTO SIRVE PARA: Reutilizar la conexión central a MySQL
include 'db_connect.php';

// ESTO SIRVE PARA: Capturar el JSON que envía la aplicación Android desde Volley
$json = file_get_contents('php://input');
$data = json_decode($json, true);

// Validación de campos obligatorios
if (empty($data['correo']) || empty($data['password']) || empty($data['nombres'])) {
    echo json_encode(["status" => "error", "message" => "Faltan datos obligatorios (nombre, correo o contraseña)."]);
    exit();
}

$nombres = trim($data['nombres']);
$correo = trim($data['correo']);
// ESTO SIRVE PARA: Encriptar la contraseña con BCrypt antes de almacenarla
$password = password_hash($data['password'], PASSWORD_BCRYPT);
$fecha_nac = isset($data['fecha_nacimiento']) ? trim($data['fecha_nacimiento']) : "";
$universidad = isset($data['universidad']) ? trim($data['universidad']) : "";
$carrera = isset($data['carrera']) ? trim($data['carrera']) : "";

// ESTO SIRVE PARA: Evitar registros duplicados con el mismo correo
$check_stmt = $conn->prepare("SELECT id FROM cuentas WHERE correo = ?");
$check_stmt->bind_param("s", $correo);
$check_stmt->execute();
$check_res = $check_stmt->get_result();

if ($check_res && $check_res->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Este correo electrónico ya se encuentra registrado."]);
    $check_stmt->close();
    exit();
}
$check_stmt->close();

// ESTO SIRVE PARA: Insertar el nuevo estudiante de manera segura con Sentencias Preparadas (evita inyecciones SQL)
$stmt = $conn->prepare("INSERT INTO cuentas (nombres, correo, password, fecha_nacimiento, universidad, carrera) VALUES (?, ?, ?, ?, ?, ?)");
if ($stmt) {
    $stmt->bind_param("ssssss", $nombres, $correo, $password, $fecha_nac, $universidad, $carrera);
    if ($stmt->execute()) {
        $inserted_id = $stmt->insert_id;

        $conn->query("INSERT IGNORE INTO configuracion_descanso (correo, hora_dormir, hora_despertar, meta_horas) VALUES ('$correo', '23:00', '07:00', '8 horas')");

        echo json_encode(["status" => "success", "message" => "Cuenta registrada correctamente.", "id" => $inserted_id]);
    } else {
        echo json_encode(["status" => "error", "message" => "Error al registrar la cuenta: " . $stmt->error]);
    }
    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Error de preparación: " . $conn->error]);
}

$conn->close();
?>
