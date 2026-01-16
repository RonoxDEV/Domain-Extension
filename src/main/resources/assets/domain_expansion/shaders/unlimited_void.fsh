precision mediump float;

varying vec2 vUV;

uniform sampler2D Sampler0; // void_background
uniform sampler2D Sampler1; // infinite_noise
uniform float u_Time;
uniform vec3 u_Color; // For potential tinting

void main() {
    // Layer 1: Background (Stars)
    vec4 bgColor = texture2D(Sampler0, vUV);
    
    // Layer 2 & 3: Flux & Distortion (Gojo's secret)
    // Scrolling noise in different directions and speeds
    vec2 flowUV = vUV + vec2(u_Time * 0.05, u_Time * 0.02);
    vec2 distortUV = vUV + vec2(u_Time * -0.03, u_Time * 0.05);
    
    vec4 noise1 = texture2D(Sampler1, flowUV);
    vec4 noise2 = texture2D(Sampler1, distortUV + noise1.rg * 0.1);
    
    // Combine noise for "information overflow" effect
    float noiseImpact = (noise1.r + noise2.r) * 0.5;
    
    // Fresnel-like effect for edge glow
    float fresnel = pow(1.0 - abs(dot(normalize(vUV - 0.5), vec2(0.0, 1.0))), 3.0);
    
    // Color composition: Noir, Blanc, Cyan léger
    vec3 cyan = vec3(0.8, 0.9, 1.0);
    vec3 finalColor = mix(bgColor.rgb, cyan, noiseImpact * 0.6);
    finalColor = mix(finalColor, vec3(1.0), noiseImpact * noiseImpact * 0.8); // Highlights
    
    float alpha = 0.8 + fresnel * 0.2;
    
    gl_FragColor = vec4(finalColor, alpha);
}
