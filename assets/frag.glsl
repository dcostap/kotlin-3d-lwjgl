#version 400 core

//in vec2 out_texture_coords;
in vec4 out_color;

out vec4 final_color;

// uniform sampler2D textureSampler;

void main(void) {
    // out_color = texture(textureSampler, out_texture_coords);
    final_color = vec4(1.0, 0.0, 0.0, 1.0);//vec4(0.0, 0.0, 0.0, 1.0);
}
