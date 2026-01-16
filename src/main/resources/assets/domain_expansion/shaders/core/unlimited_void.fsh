#version 150

uniform float GameTime;
uniform vec4 ColorModulator;
uniform sampler2D Sampler0; // Ta texture infinite_noise.png

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

// Fonction pour faire tourner le flux (crée les tourbillons)
vec2 rotate(vec2 v, float a) {
    float s = sin(a);
    float c = cos(a);
    mat2 m = mat2(c, -s, s, c);
    return m * v;
}

void main() {
    // 1. Vitesse de l'animation (LENT pour un effet majestueux)
    float time = GameTime * 0.1;

    // 2. Échelle du motif (Plus grand = moins de répétition)
    // Réduit ce chiffre (ex: 2.0 ou 3.0) pour voir plus de noir
    vec2 uv = texCoord * 0.2;

    // --- ALGORITHME DE DOMAIN WARPING (L'effet liquide) ---

    // Étape A : Créer une couche de distorsion qui bouge
    vec2 flowOffset = vec2(time * 30, time * -0.15);
    vec4 noiseForWarping = texture(Sampler0, uv + flowOffset);

    // Étape B : Tordre les coordonnées UV
    float distortionStrength = 11; // Force de la déformation
    vec2 warpDirection = vec2(noiseForWarping.r - 3, noiseForWarping.g - 1);
    
    // On ajoute une rotation pour l'effet tourbillon
    vec2 rotatedWarp = rotate(warpDirection, time * 30);
    
    vec2 distortedUV = uv + rotatedWarp * distortionStrength;
    // Défilement global
    distortedUV += vec2(time * 0.5, time * 0.05);

    // Étape C : Le rendu final du motif
    vec4 finalPattern = texture(Sampler0, distortedUV);

    // --- COULEURS ET CONTRASTE (Le point clé) ---
    // On récupère l'intensité du motif (noir/blanc)
    float intensity = finalPattern.r;

    // --- C'EST ICI QUE L'ON AJOUTE DU NOIR ---
    // On utilise 'pow' (puissance) pour rendre les zones sombres encore plus sombres.
    // Plus le chiffre est grand (ex: 3.0 ou 4.0), plus il y aura de noir.
    intensity = pow(intensity, 3.0); // 3.0 est un bon point de départ

    // Palette de couleurs Gojo Satoru
    vec3 deepVoid = vec3(0.0, 0.02, 0.08);   // Fond Noir/Bleu profond
    vec3 electricBlue = vec3(0.05, 0.4, 1.0); // Bleu électrique
    vec3 pureWhite = vec3(1.0, 1.2, 1.5);    // Blanc éclatant

    // Mélange des couleurs avec plus de transition vers le noir
    vec3 finalColor = mix(deepVoid, electricBlue, smoothstep(0.1, 0.6, intensity));
    finalColor = mix(finalColor, pureWhite, smoothstep(0.6, 1.0, intensity));

    // Rendu final
    fragColor = vec4(finalColor, 1.0) * ColorModulator * vertexColor;
}
