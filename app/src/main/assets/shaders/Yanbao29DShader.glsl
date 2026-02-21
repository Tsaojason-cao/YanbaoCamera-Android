// Yanbao_29D_Mapper.frag
// yanbao AI 核心 GPU Shader
// 實時映射 29D 參數矩陣到取景器畫面

precision highp float;

// 輸入紋理（來自相機預覽）
uniform sampler2D inputTexture;

// 29D 參數數組（從 JSON metadata 讀取）
uniform float params29D[29];

// 紋理坐標
varying vec2 vTexCoord;

// === 工具函數 ===

// 隨機噪聲生成器（用於顆粒感）
float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

// 色彩矩陣乘法
vec3 applyColorMatrix(vec3 color, mat3 matrix) {
    return matrix * color;
}

// === 主渲染函數 ===

void main() {
    // 讀取原始像素
    vec4 color = texture2D(inputTexture, vTexCoord);
    
    // ==========================================
    // D1-D5: 物理光影映射
    // ==========================================
    
    // D1: 曝光 (Exposure)
    color.rgb *= params29D[0];
    
    // D2: 高光壓縮 (Highlight Compression)
    float highlightThreshold = 0.8;
    vec3 highlights = max(color.rgb - highlightThreshold, 0.0);
    color.rgb -= highlights * params29D[1];
    
    // D3: 暗部補償 (Shadow Boost)
    float shadowThreshold = 0.2;
    vec3 shadows = max(shadowThreshold - color.rgb, 0.0);
    color.rgb += shadows * params29D[2];
    
    // D4: 冷暖偏移 (Temperature)
    color.r += params29D[3] * 0.1;
    color.b -= params29D[3] * 0.1;
    
    // D5: 色偏 (Tint)
    color.g += params29D[4] * 0.1;
    
    // ==========================================
    // D6-D15: 色彩空間映射
    // ==========================================
    
    // 構建 3x3 色彩矩陣（從 D6-D14）
    mat3 colorMatrix = mat3(
        params29D[5],  params29D[6],  params29D[7],
        params29D[8],  params29D[9],  params29D[10],
        params29D[11], params29D[12], params29D[13]
    );
    
    // 應用色彩矩陣變換
    color.rgb = applyColorMatrix(color.rgb, colorMatrix);
    
    // D15: 紅色純度增強（徠卡風格）
    color.r += params29D[14] * 0.15;
    
    // ==========================================
    // D16-D25: 物理紋理映射
    // ==========================================
    
    // D16: 整體銳化（預留）
    // float sharpen = params29D[15];
    
    // D17: 邊緣檢測（預留）
    // float edge = params29D[16];
    
    // D18: 顆粒感 (Film Grain)
    float grainIntensity = params29D[17];
    float noise = rand(vTexCoord + fract(params29D[0])) * grainIntensity;
    color.rgb += vec3(noise);
    
    // D19: 高光拉絲（賽博霓虹效果）
    float glowIntensity = params29D[18];
    vec3 glow = max(color.rgb - 0.9, 0.0) * glowIntensity;
    color.rgb += glow;
    
    // D20-D21: 預留
    
    // D22: 色散補償 (Chromatic Aberration Compensation)
    float aberration = params29D[21];
    vec2 offset = vec2(aberration * 0.01, 0.0);
    color.r = texture2D(inputTexture, vTexCoord + offset).r;
    color.b = texture2D(inputTexture, vTexCoord - offset).b;
    
    // D23-D25: 預留（衍射、銳化、邊緣衰減）
    
    // ==========================================
    // D26-D29: AI 骨骼/空間映射
    // ==========================================
    
    // D26: 臉部折疊度（預留，需要 AI 骨骼點數據）
    // D27: 骨骼比例（預留）
    // D28: 空間深度（預留）
    // D29: LBS 環境光補償
    float lbsCompensation = params29D[28];
    color.rgb += vec3(lbsCompensation * 0.05);
    
    // ==========================================
    // 最終輸出
    // ==========================================
    
    // 確保顏色範圍在 [0, 1]
    color.rgb = clamp(color.rgb, 0.0, 1.0);
    
    gl_FragColor = color;
}
