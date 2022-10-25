use crate::{Hittable, Ray, Vec3};
use crate::aabb::AABB;
use crate::hittable::HitRecord;
use crate::material::Material;

pub enum PlaneType {
    XY,
    XZ,
    YZ,
}

pub struct Plane<M: Material> {
    pub plane_type: PlaneType,
    pub a_dim: DimConfig,
    pub b_dim: DimConfig,
    pub p: f64,
    pub p_index: usize,
    pub material: M,
}

pub struct DimConfig {
    l_bound: f64,
    r_bound: f64,
    index: usize,
}

impl DimConfig {
    fn new(l_bound: f64, r_bound: f64, index: usize) -> Self {
        Self { l_bound, r_bound, index }
    }

    fn in_bounds(&self, point: &Vec3) -> bool {
        self.l_bound <= point[self.index] && point[self.index] <= self.r_bound
    }

    fn coord(&self, point: &Vec3) -> f64 {
        (point[self.index] - self.l_bound) / (self.r_bound - self.l_bound)
    }
}

impl<M: Material> Plane<M> {
    pub fn new(
        plane_type: PlaneType,
        a0: f64, a1: f64,
        b0: f64, b1: f64,
        p: f64,
        material: M,
    ) -> Self {
        let (a_index, b_index, p_index) = match plane_type {
            PlaneType::XY => (0, 1, 2),
            PlaneType::XZ => (0, 2, 1),
            PlaneType::YZ => (1, 2, 0),
        };
        Self {
            plane_type,
            a_dim: DimConfig::new(a0, a1, a_index),
            b_dim: DimConfig::new(b0, b1, b_index),
            p,
            p_index,
            material,
        }
    }

    fn in_bounds(&self, point: &Vec3) -> bool {
        self.a_dim.in_bounds(point) && self.b_dim.in_bounds(point)
    }

    fn factor(&self, ray: &Ray) -> f64 {
        (self.p - ray.origin[self.p_index]) / ray.dir[self.p_index]
    }
}

impl<M: Material> Hittable for Plane<M> {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let t = self.factor(ray);
        if !self.in_bounds(&ray.at(t)) || !(t_min <= t && t <= t_max) {
            return None;
        }
        self.get_hit_record(ray, t)
    }

    fn bounding_box(&self) -> Option<AABB> {
        Some(AABB::new(
            Vec3::new(self.a_dim.l_bound, self.b_dim.l_bound, self.p - 0.0001),
            Vec3::new(self.a_dim.r_bound, self.b_dim.r_bound, self.p + 0.0001),
        ))
    }

    fn get_normal(&self, _point: &Vec3) -> Vec3 {
        match self.plane_type {
            PlaneType::XY => Vec3::new(0.0, 0.0, 1.0),
            PlaneType::XZ => Vec3::new(0.0, 1.0, 0.0),
            PlaneType::YZ => Vec3::new(1.0, 0.0, 0.0),
        }
    }

    fn get_coordinates(&self, point: &Vec3) -> (f64, f64) {
        (self.a_dim.coord(point), self.b_dim.coord(point))
    }

    fn get_material(&self) -> &dyn Material {
        &self.material
    }
}
