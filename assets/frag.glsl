#version 400 core

in vec2 out_texture_coords;
out vec4 out_color;

// uniform sampler2D textureSampler;

void main(void) {
    // out_color = texture(textureSampler, out_texture_coords);
    out_color = vec4(0.0, 0.0, 0.0, 1.0);
}
